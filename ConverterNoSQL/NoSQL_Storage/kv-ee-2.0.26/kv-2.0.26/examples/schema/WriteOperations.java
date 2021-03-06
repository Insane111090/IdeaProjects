/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package schema;

import java.util.List;
import java.util.concurrent.TimeUnit;

import oracle.kv.Depth;
import oracle.kv.Durability;
import oracle.kv.DurabilityException;
import oracle.kv.FaultException;
import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.Key;
import oracle.kv.KeyRange;
import oracle.kv.Operation;
import oracle.kv.OperationExecutionException;
import oracle.kv.OperationResult;
import oracle.kv.RequestTimeoutException;
import oracle.kv.ReturnValueVersion;
import oracle.kv.Value;
import oracle.kv.Version;

/**
 * Performs write operations and retries the operation when a {@link
 * FaultException} occurs in order to handle transient network failures.  While
 * the {@link KVStore} read methods perform retries automatically when a
 * network failure occurs, the {@link KVStore} write methods do not.
 * Therefore, it is recommended that write operations are performed using the
 * methods in this class, or using a similar retry mechanism in the
 * application.
 * <p>
 * The KVStore write methods are not defined to be strictly idempotent which
 * means if they are performed more than once, the outcome may be different
 * than if they were performed a single time. Repeating an operation may occur
 * when it is retried after a network failure, if in fact the operation was
 * successful the first time. Because of the network failure, the client is
 * unaware of whether the first operation failed or succeeded.
 * <p>
 * For example, if {@link KVStore#delete KVStore.delete} throws a
 * FaultException due to a network failure, it may or may not have succeeded.
 * Imagine that it did succeed and the network failure occurred when receiving
 * the operation reply message.  When the KVStore.delete call is retried, it
 * will return false because the previous attempt succeeded.  Of course, this
 * can also occur if another client deletes the record, even when no retries
 * are necessary.  Therefore, the semantics of the {@link #delete delete}
 * method in this class are slightly different than the semantics of {@link
 * KVStore#delete KVStore.delete}.  The delete method here does not return an
 * indication of whether it deleted the record.  The record is guaranteed to be
 * deleted when the delete method returns without an exception, but there is no
 * way to know whether it was deleted by this method or another client.  With
 * this change in semantics, the delete method in this class is idempotent.
 * <p>
 * Several methods in this class fall into the same category as the delete
 * method in that they are idempotent, as they are defined here.  These are:
 * {@link #put put}, {@link #delete delete} and {@link #multiDelete
 * multiDelete}.  When these methods return without an exception, their outcome
 * is always the same, whether or not retries were necessary. For that reason,
 * retrying these methods at the application level is not needed when no
 * exception is thrown.
 * <p>
 * Most of the other methods in this class -- {@link #putIfAbsent putIfAbsent},
 * {@link #putIfPresent putIfPresent}, {@link #putIfVersion putIfVersion} and
 * {@link #deleteIfVersion deleteIfVersion} -- fall into a different category.
 * These methods are not idempotent, even as defined here, because the
 * application needs to decide whether the operations need to be retried in the
 * face of concurrent access.
 * <p>
 * For example, say the application performs a {@link KVStore#get KVStore.get},
 * examines the record value and determines that it qualifies for deletion, and
 * then calls {@link #deleteIfVersion deleteIfVersion}.  Say the call to
 * KVStore.deleteIfVersion succeeds (when first called by the deleteIfVersion
 * method here), but throws a FaultException due to a network failure. When the
 * KVStore.deleteIfVersion call is retried, it returns false because the
 * previous attempt succeeded and the record was deleted as a result; the
 * deleteIfVersion method in this class will return false as well.  Of course,
 * this can also occur if another client has deleted or modified the record,
 * even when no retries are necessary.  Therefore, the meaning of a false
 * return value from the deleteIfVersion method in this class is slightly
 * different than for KVStore.deleteIfVersion.  When false is returned by the
 * deleteIfVersion method here, it may be because another client deleted or
 * modified the record, or because <em>this method</em> itself unknowingly
 * deleted the record and then retried the operation.  Either way, the
 * application should normally retry the higher level operation: call
 * KVStore.get again (or use the prevValue parameter of the deleteIfVersion
 * method) to get the current record value, and examine it again to see if the
 * record still qualifies for deletion.  In the example described, the record
 * will no longer exist and the application should assume that it was deleted
 * by another client or by this method itself; these two cases cannot be
 * distinguished.
 * <p>
 * As an illustration of the difference in semantics, imagine a store that is
 * idle except for a single client thread that is performing KVStore.get and
 * deleteIfVersion calls (the method in this class).  We also guarantee that no
 * data migration takes place in this test, since data migration changes record
 * versions as if another client performed an update.  In this test, one might
 * assume that the deleteIfVersion method should always return true, since no
 * other clients are accessing the store.  However, this is an incorrect
 * assumption.  If the test is run for long enough, transient network failures
 * will eventually occur, and deleteIfVersion will return false due to
 * scenarios such as the one described above.  This may be an unexpected
 * outcome in such a test scenario, but should be expected in a real world
 * application due to other client activity and data migration, as well as
 * network failures.
 * <p>
 * The following example is also noteworthy.  Imagine that {@link #putIfVersion
 * putIfVersion} is used to increment or decrement a bank balance, or make
 * another sort of incremental or conditional update.  If null is returned by
 * the putIfVersion method in this class (or if {@link KVStore#putIfVersion
 * KVStore.putIfVersion} throws a FaultException), this means the operation may
 * or may not have succeeded.  If performing the change exactly once is
 * critical, as it would be when incrementing or decrementing a bank balance,
 * the application must build in some means of determining whether the change
 * succeeded or not.  This explains why putIfVersion and similar methods in
 * this class cannot simply compare the currently stored value to the value
 * requested, to determine whether the operation succeeded.  The test for
 * success or failure must be left to the application in such cases.
 * <p>
 * The execute method is a special case, since it doesn't fall neatly into one
 * of the two categories defined: idempotent like delete, or non-idempotent
 * like deleteIfVersion.  This is because execute can be used to perform a
 * combination of delete and deleteIfVersion, as well as other types of write
 * operations.  The execute method in this class will retry KVStore.execute
 * when a FaultException is thrown, and the meaning of the individual operation
 * results will depend on the type of operation, as defined by the other
 * methods in this class.
 * <p>
 * Note that this class does not do any exception handling, other than to retry
 * the operation after any kind of FaultException is thrown.  Up to {@link
 * #N_RETRIES} (currently two) retries are performed, and a FaultException that
 * occurs in the last attempt is propagated to the caller.  The caller should
 * handle the exception as described in the {@link RunOperation} class in this
 * example.  In this example, calls to methods in this class are always made
 * within the context of a RunOperation execution, and exceptions are handled
 * by RunOperation in all cases.
 * <p>
 * A known deficiency of this class is that a network failure is not
 * distinguished from other types of FaultExceptions that might occur; a retry
 * is performed when any type of FaultException is thrown.  This is not
 * considered a major problem for two reasons: a) other types of failures are
 * likely to be persistent and will quickly occur again when retrying, and b)
 * optimizing performance when handling FaultExceptions is not normally a
 * concern.
 */
@SuppressWarnings("javadoc")
class WriteOperations {

    /**
     * Maximum number of retries to perform.
     */
    private static final int N_RETRIES = 2;

    /**
     * The delay prior to each retry in milliseconds.
     */
    private static final int DELAY_MS = 10;

    /**
     * The underlying KVStore used to perform operations.
     */
    private final KVStore store;

    /**
     * The default operation timeout in milliseconds.
     */
    private final long defaultTimeoutMs;

    /**
     * Creates a WriteOperations wrapper for a given KVStore.
     */
    WriteOperations(final KVStore store, final KVStoreConfig config) {
        this.store = store;
        defaultTimeoutMs = config.getRequestTimeout(TimeUnit.MILLISECONDS);
    }

    /**
     * Calls {@link KVStore#put(Key, Value) KVStore.put} and performs retries
     * if a FaultException is thrown.
     * <p>
     * This method is equivalent to {@link #put(Key, Value, ReturnValueVersion,
     * Durability, long, TimeUnit)} except that the prevValue, durability,
     * timeout and timeoutUnit parameters are not specified and take on default
     * values.
     */
    public Version put(final Key key, final Value value)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return put(key, value, null, null, 0, null);
    }

    /**
     * Calls {@link KVStore#put(Key, Value) KVStore.put} and performs retries
     * if a FaultException is thrown.
     * <p>
     * This method is idempotent in the sense that if it is called multiple
     * times and returns without throwing an exception, the outcome is always
     * the same:  the given Key/Value pair will have been stored.
     */
    public Version put(final Key key,
                       final Value value,
                       final ReturnValueVersion prevValue,
                       final Durability durability,
                       final long timeout,
                       final TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return new WriteOp<Version, RuntimeException>(timeout, timeoutUnit) {
            @Override
            Version doWrite(final long timeoutMs) {
                return store.put(key, value, prevValue, durability, timeoutMs,
                                 TimeUnit.MILLISECONDS);
            }
        }.run();
    }

    /**
     * Calls {@link KVStore#putIfAbsent KVStore.putIfAbsent} and performs
     * retries if a FaultException is thrown.
     * <p>
     * This method is equivalent to {@link #putIfAbsent(Key, Value,
     * ReturnValueVersion, Durability, long, TimeUnit)} except that the
     * prevValue, durability, timeout and timeoutUnit parameters are not
     * specified and take on default values.
     */
    public Version putIfAbsent(final Key key,
                               final Value value)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return putIfAbsent(key, value, null, null, 0, null);
    }

    /**
     * Calls {@link KVStore#putIfAbsent KVStore.putIfAbsent} and performs
     * retries if a FaultException is thrown.
     * <p>
     * This method is not idempotent since if it is called multiple times, the
     * outcome may be different because the key may or may not exist.  When
     * null is returned, the application is expected to take action, such as
     * performing retries, at a higher level.
     * <p>
     * When a retry is performed by this method and it returns null because the
     * key is present, there is no returned indication of whether the key was
     * inserted by an earlier attempt in the same method invocation, or by
     * another client.  The application must be prepared for either case.
     * <p>
     * Because of this ambiguity, it can be difficult to use this method
     * (instead of put) as a self-check when the key is expected to be absent,
     * or as a way to prevent two clients from writing the same key.  To do
     * this reliably, each client must include a unique identifier in the value
     * and check for that identifier (call KVStore.get or use the prevValue
     * parameter of this method) when null is returned.
     */
    public Version putIfAbsent(final Key key,
                               final Value value,
                               final ReturnValueVersion prevValue,
                               final Durability durability,
                               final long timeout,
                               final TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return new WriteOp<Version, RuntimeException>(timeout, timeoutUnit) {
            @Override
            Version doWrite(final long timeoutMs) {
                return store.putIfAbsent(key, value, prevValue, durability,
                                         timeoutMs, TimeUnit.MILLISECONDS);
            }
        }.run();
    }

    /**
     * Calls {@link KVStore#putIfPresent KVStore.putIfPresent} and performs
     * retries if a FaultException is thrown.
     * <p>
     * This method is equivalent to {@link #putIfPresent(Key, Value,
     * ReturnValueVersion, Durability, long, TimeUnit)} except that the
     * prevValue, durability, timeout and timeoutUnit parameters are not
     * specified and take on default values.
     */
    public Version putIfPresent(final Key key,
                                final Value value)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return putIfPresent(key, value, null, null, 0, null);
    }

    /**
     * Calls {@link KVStore#putIfPresent KVStore.putIfPresent} and performs
     * retries if a FaultException is thrown.
     * <p>
     * This method is not idempotent since if it is called multiple times, the
     * outcome may be different because the key may or may not exist.  When
     * null is returned, the application is expected to take action, such as
     * performing retries, at a higher level.
     * <p>
     * This method is commonly used (instead of put) as a self-check, when the
     * key is expected to be present.
     */
    public Version putIfPresent(final Key key,
                                final Value value,
                                final ReturnValueVersion prevValue,
                                final Durability durability,
                                final long timeout,
                                final TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return new WriteOp<Version, RuntimeException>(timeout, timeoutUnit) {
            @Override
            Version doWrite(final long timeoutMs) {
                return store.putIfPresent(key, value, prevValue, durability,
                                          timeoutMs, TimeUnit.MILLISECONDS);
            }
        }.run();
    }

    /**
     * Calls {@link KVStore#putIfVersion KVStore.putIfVersion} and performs
     * retries if a FaultException is thrown.
     * <p>
     * This method is equivalent to {@link #putIfVersion(Key, Value, Version,
     * ReturnValueVersion, Durability, long, TimeUnit)} except that the
     * prevValue, durability, timeout and timeoutUnit parameters are not
     * specified and take on default values.
     */
    public Version putIfVersion(final Key key,
                                final Value value,
                                final Version matchVersion)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return putIfVersion(key, value, matchVersion, null, null, 0, null);
    }

    /**
     * Calls {@link KVStore#putIfVersion KVStore.putIfVersion} and performs
     * retries if a FaultException is thrown.
     * <p>
     * This method is not idempotent since if it is called multiple times, the
     * outcome may be different because the version may or may not match.  When
     * null is returned, the application is expected to take action, such as
     * performing retries, at a higher level.
     * <p>
     * When a retry is performed by this method and it returns null because the
     * version does not match, there is no returned indication of whether the
     * version was changed by an earlier attempt in the same method invocation,
     * or by another client.  The application must be prepared for either case.
     * <p>
     * This method is commonly used (instead of put) as a way to avoid lost
     * updates.
     * <p>
     * WARNING: A putIfVersion operation should not be used to perform a
     * self-check because the KVStore system may internally assign a new
     * Version to a Key/Value pair when migrating data for better resource
     * usage.  One should never assume that only the application can change the
     * Version of a Key/Value pair.
     */
    public Version putIfVersion(final Key key,
                                final Value value,
                                final Version matchVersion,
                                final ReturnValueVersion prevValue,
                                final Durability durability,
                                final long timeout,
                                final TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return new WriteOp<Version, RuntimeException>(timeout, timeoutUnit) {
            @Override
            Version doWrite(final long timeoutMs) {
                return store.putIfVersion(key, value, matchVersion, prevValue,
                                          durability, timeoutMs,
                                          TimeUnit.MILLISECONDS);
            }
        }.run();
    }

    /**
     * Calls {@link KVStore#delete(Key) KVStore.delete} and performs retries
     * if a FaultException is thrown.
     * <p>
     * This method is equivalent to {@link #delete(Key, ReturnValueVersion,
     * Durability, long, TimeUnit)} except that the prevValue, durability,
     * timeout and timeoutUnit parameters are not specified and take on default
     * values.
     */
    public void delete(final Key key)
        throws DurabilityException, RequestTimeoutException, FaultException {

        delete(key, null, null, 0, null);
    }

    /**
     * Calls {@link KVStore#delete(Key) KVStore.delete} and performs retries
     * if a FaultException is thrown.
     * <p>
     * This method is idempotent in the sense that if it is called multiple
     * times and returns without throwing an exception, the outcome is always
     * the same:  the given Key/Value pair will have been deleted.
     * <p>
     * Unlike {@link KVStore#delete(Key) KVStore.delete}, this method does not
     * return any indication of whether the Key/Value pair was deleted by this
     * method or by another client.
     */
    public void delete(final Key key,
                       final ReturnValueVersion prevValue,
                       final Durability durability,
                       final long timeout,
                       final TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException {

        new WriteOp<Void, RuntimeException>(timeout, timeoutUnit) {
            @Override
            Void doWrite(final long timeoutMs) {
                store.delete(key, prevValue, durability, timeoutMs,
                             TimeUnit.MILLISECONDS);
                return null;
            }
        }.run();
    }

    /**
     * Calls {@link KVStore#deleteIfVersion KVStore.deleteIfVersion} and
     * performs retries if a FaultException is thrown.
     * <p>
     * This method is equivalent to {@link #deleteIfVersion(Key, Version,
     * ReturnValueVersion, Durability, long, TimeUnit)} except that the
     * prevValue, durability, timeout and timeoutUnit parameters are not
     * specified and take on default values.
     */
    public boolean deleteIfVersion(final Key key,
                                   final Version matchVersion)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return deleteIfVersion(key, matchVersion, null, null, 0, null);
    }

    /**
     * Calls {@link KVStore#deleteIfVersion KVStore.deleteIfVersion} and
     * performs retries if a FaultException is thrown.
     * <p>
     * This method is not idempotent since if it is called multiple times, the
     * outcome may be different because the version may or may not match.  When
     * false is returned, the application is expected to take action, such as
     * performing retries, at a higher level.
     * <p>
     * When a retry is performed by this method and it returns false because
     * the version does not match, there is no returned indication of whether
     * the version was changed by an earlier attempt in the same method
     * invocation, or by another client.  The application must be prepared for
     * either case.
     * <p>
     * This method is commonly used (instead of delete) as a way to avoid lost
     * updates.
     * <p>
     * WARNING: A deleteIfVersion operation should not be used to perform a
     * self-check because the KVStore system may internally assign a new
     * Version to a Key/Value pair when migrating data for better resource
     * usage.  One should never assume that only the application can change the
     * Version of a Key/Value pair.
     */
    public boolean deleteIfVersion(final Key key,
                                   final Version matchVersion,
                                   final ReturnValueVersion prevValue,
                                   final Durability durability,
                                   final long timeout,
                                   final TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException {

        return new WriteOp<Boolean, RuntimeException>(timeout, timeoutUnit) {
            @Override
            Boolean doWrite(final long timeoutMs) {
                return store.deleteIfVersion(key, matchVersion, prevValue,
                                             durability, timeoutMs,
                                             TimeUnit.MILLISECONDS);
            }
        }.run();
    }

    /**
     * Calls {@link KVStore#multiDelete(Key, KeyRange, Depth)
     * KVStore.multiDelete} and performs retries if a FaultException is thrown.
     * <p>
     * This method is equivalent to {@link #multiDelete(Key, KeyRange, Depth,
     * Durability, long, TimeUnit)} except that the durability, timeout and
     * timeoutUnit parameters are not specified and take on default values.
     */
    public void multiDelete(final Key parentKey,
                            final KeyRange subRange,
                            final Depth depth)
        throws DurabilityException, RequestTimeoutException, FaultException {

        multiDelete(parentKey, subRange, depth, null, 0, null);
    }

    /**
     * Calls {@link KVStore#multiDelete(Key, KeyRange, Depth)
     * KVStore.multiDelete} and performs retries if a FaultException is thrown.
     * <p>
     * This method is idempotent in the sense that if it is called multiple
     * times and returns without throwing an exception, the outcome is always
     * the same:  the specified Key/Value pairs will have been deleted.
     * <p>
     * Unlike {@link KVStore#multiDelete(Key, KeyRange, Depth)
     * KVStore.multiDelete}, this method does not return any indication of how
     * many Key/Value pairs were deleted by this method.
     */
    public void multiDelete(final Key parentKey,
                            final KeyRange subRange,
                            final Depth depth,
                            final Durability durability,
                            final long timeout,
                            final TimeUnit timeoutUnit)
        throws DurabilityException, RequestTimeoutException, FaultException {

        new WriteOp<Void, RuntimeException>(timeout, timeoutUnit) {
            @Override
            Void doWrite(final long timeoutMs) {
                store.multiDelete(parentKey, subRange, depth, durability,
                                  timeoutMs, TimeUnit.MILLISECONDS);
                return null;
            }
        }.run();
    }

    /**
     * Calls {@link KVStore#execute KVStore.execute} and performs retries if a
     * FaultException is thrown.
     * <p>
     * This method is equivalent to {@link #execute(List, Durability, long,
     * TimeUnit)} except that the durability, timeout and timeoutUnit
     * parameters are not specified and take on default values.
     */
    public List<OperationResult> execute(final List<Operation> operations)
        throws OperationExecutionException,
               DurabilityException,
               FaultException {

        return execute(operations, null, 0, null);
    }

    /**
     * Calls {@link KVStore#execute KVStore.execute} and performs retries if a
     * FaultException is thrown.
     * <p>
     * This method may or may not be idempotent since the specified operations
     * may or may not be idempotent.  Care should be taken when multiple
     * non-idempotent operations are included, because retries may cause some
     * operations to fail.
     * <p>
     * When a retry is performed by this method and an {@link
     * OperationExecutionException} is thrown, there is no returned indication
     * of whether the operation(s) failed due to an operation that succeeded in
     * an earlier attempt in the same method invocation, or due to an operation
     * by another client.  The application must be prepared for either case.
     */
    public List<OperationResult> execute(final List<Operation> operations,
                                         final Durability durability,
                                         final long timeout,
                                         final TimeUnit timeoutUnit)
        throws OperationExecutionException,
               DurabilityException,
               FaultException {

        return new WriteOp<List<OperationResult>,
                           OperationExecutionException>(timeout, timeoutUnit) {
            @Override
            List<OperationResult> doWrite(final long timeoutMs)
                throws OperationExecutionException {

                return store.execute(operations, durability, timeoutMs,
                                     TimeUnit.MILLISECONDS);
            }
        }.run();
    }

    /**
     * Internal class used to perform retries for a write operation.
     */
    abstract class WriteOp<R, E extends Exception> {
        private long timeoutMs;
        private final long endTime;

        /**
         * Creates the write operation with the requested timeout parameters.
         */
        WriteOp(final long timeout, final TimeUnit timeoutUnit) {
            timeoutMs = (timeout > 0) ?
                         timeoutUnit.toMillis(timeout) :
                         defaultTimeoutMs;
            endTime = System.currentTimeMillis() + timeoutMs;
        }

        /**
         * Implemented for each write operation.
         *
         * @param tmOutMS the timeout in milliseconds to use for this attempt
         * of the write operation.
         */
        abstract R doWrite(long tmOutMS) throws FaultException, E;

        /**
         * Calls the doWrite method and perform retries when a FaultException
         * is thrown.
         */
        R run() throws FaultException, E {
            for (int i = 0; true; i += 1) {
                try {
                    return doWrite(timeoutMs);
                } catch (final FaultException fe) {
                    /* Throw the fault exception if max retries is exceeded. */
                    if (i >= N_RETRIES) {
                        throw fe;
                    }
                    /* Delay before the retry, if there is enough time left. */
                    long now = System.currentTimeMillis();
                    final long delayMs =
                        Math.min(DELAY_MS, (endTime - now) - 1);
                    if (delayMs > 0) {
                        try {
                            Thread.sleep(delayMs);
                        } catch (final InterruptedException ie) {
                            /* Don't swallow the interrupt status. */
                            Thread.currentThread().interrupt();
                            throw fe;
                        }
                        now = System.currentTimeMillis();
                    }
                    /* Adjust the timeout before retrying. */
                    timeoutMs = endTime - now;
                    /* Throw the fault exception if the timeout is exceeded. */
                    if (timeoutMs <= 0) {
                        throw fe;
                    }
                    /* Retry with the adjusted timeout. */
                }
            }
        }
    }
}
