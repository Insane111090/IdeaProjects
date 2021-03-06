/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package schema;

import oracle.kv.Key;
import oracle.kv.Value;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

/**
 * Holds the session summary attributes that are stored as the {@code
 * Value} for the "/user/EMAIL/-/login" {@code Key}.  Illustrates the
 * use of a Key/Value pair for a parent (non-leaf) {@code Key}.
 *
 * <p>In general, be aware that there are performance trade-offs in
 * storing summary data, as shown in this example, versus computing it
 * using a query when it is needed.  For example, we could avoid
 * storing {@code totalLoginCount} and get the count of events
 * efficiently using a range query on the keys. But we would still
 * need to maintain the {@code totalLoginDuration} if we wanted to
 * avoid accessing each {@code LoginSession} to sum the individual
 * values on demand.  In some cases, summary data may be expensive
 * enough to recompute that it's worth storing. This is a trade-off
 * that application designers need to make based upon their knowledge
 * of application access patterns.</p>
 */
class LoginSummary {

    /*
     * The email address is a unique identifier and is used to construct
     * the Key's major path.
     */
    private final String email;

    /* Persistent fields stored in the Value. */
    private int totalLoginCount;
    private long totalLoginDuration;

    /**
     * Constructs a user object with its unique identifier, the email address.
     */
    LoginSummary(String email) {
        this.email = email;
    }

    /**
     * Returns the email identifier.
     */
    String getEmail() {
        return email;
    }

    /**
     * Changes the total login count.
     */
    void setTotalLoginCount(int totalLoginCount) {
        this.totalLoginCount = totalLoginCount;
    }

    /**
     * Returns the total login count.
     */
    int getTotalLoginCount() {
        return totalLoginCount;
    }

    /**
     * Changes the total login duration.
     */
    void setTotalLoginDuration(long totalLoginDuration) {
        this.totalLoginDuration = totalLoginDuration;
    }

    /**
     * Returns the total login duration.
     */
    long getTotalLoginDuration() {
        return totalLoginDuration;
    }

    /**
     * Returns a Key that can be used to write or read the LoginSummary.
     */
    Key getStoreKey() {
        return KeyDefinition.makeLoginSummaryKey(email);
    }

    /**
     * Serializes the summary attributes into the byte array of a Value.
     */
    Value getStoreValue(Bindings bindings) {
        final GenericRecord rec =
            new GenericData.Record(bindings.getLoginSummarySchema());
        rec.put("totalLoginCount", totalLoginCount);
        rec.put("totalLoginDuration", totalLoginDuration);
        return bindings.getLoginSummaryBinding().toValue(rec);
    }

    /**
     * Deserializes the summary attributes from the byte array of a Value.
     */
    void setStoreValue(Bindings bindings, Value value) {
        final GenericRecord rec =
            bindings.getLoginSummaryBinding().toObject(value);
        totalLoginCount = (Integer) rec.get("totalLoginCount");
        totalLoginDuration = (Long) rec.get("totalLoginDuration");
    }

    @Override
    public String toString() {
        return "<LoginSummary " + email +
               "\n    totalLoginCount: " + totalLoginCount +
               ", totalLoginDuration: " +
               KeyDefinition.formatDuration(totalLoginDuration) +
               ">";
    }
}
