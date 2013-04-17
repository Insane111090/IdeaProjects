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
 * Holds the session duration attribute that is stored as the {@code
 * Value} for the "/user/EMAIL/-/login/TIMESTAMP" {@code Key}.
 * Illustrates the use of a timestamp as a {@code Key} component.  The
 * email and timestamp uniquely identify the login event and the user
 * session.
 */
class LoginSession {

    /*
     * The email address is a unique identifier and is used to construct
     * the Key's major path.
     */
    private final String email;

    /*
     * The login time is a unique identifier for each session and is used to
     * construct the Key's minor path.
     */
    private final long loginTime;

    /* Persistent fields stored in the Value. */
    private int sessionDuration;

    /**
     * Constructs a user object with its unique identifiers, the email address
     * and login time.
     */
    LoginSession(String email, long loginTime) {
        this.email = email;
        this.loginTime = loginTime;
    }

    /**
     * Returns the email identifier.
     */
    String getEmail() {
        return email;
    }

    /**
     * Returns the login time identifier.
     */
    long getLoginTime() {
        return loginTime;
    }

    /**
     * Changes the session duration.
     */
    void setSessionDuration(int sessionDuration) {
        this.sessionDuration = sessionDuration;
    }

    /**
     * Returns the session duration.
     */
    int getSessionDuration() {
        return sessionDuration;
    }

    /**
     * Returns a Key that can be used to write or read the LoginSession.
     */
    Key getStoreKey() {
        return KeyDefinition.makeLoginSessionKey(email, loginTime);
    }

    /**
     * Serializes the only attribute, session duration, into the byte array of
     * a Value.
     */
    Value getStoreValue(Bindings bindings) {
        final GenericRecord rec =
            new GenericData.Record(bindings.getLoginSessionSchema());
        rec.put("sessionDuration", sessionDuration);
        return bindings.getLoginSessionBinding().toValue(rec);
    }

    /**
     * Deserializes the only attribute, session duration, from the byte array
     * of a Value.
     */
    void setStoreValue(Bindings bindings, Value value) {
        final GenericRecord rec =
            bindings.getLoginSessionBinding().toObject(value);
        sessionDuration = (Integer) rec.get("sessionDuration");
    }

    @Override
    public String toString() {
        return "<LoginSession " + email +
               "\n    loginTime: " + KeyDefinition.formatTimestamp(loginTime) +
               ", sessionDuration: " +
               KeyDefinition.formatDuration(sessionDuration) +
               ">";
    }
}
