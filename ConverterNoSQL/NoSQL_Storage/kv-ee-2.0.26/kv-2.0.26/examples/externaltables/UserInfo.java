/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package externaltables;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import oracle.kv.Key;
import oracle.kv.Value;

/**
 * A simple class which represents a User in the External Tables Cookbook
 * example.
 */
class UserInfo {

    /*
     * The email address is a unique identifier and is used to construct
     * the Key's major path.
     */
    private final String email;

    /* Persistent fields stored in the Value. */
    private String name;
    private char gender;
    private String address;
    private String phone;

    private static final String INFO_PROPERTY_NAME = "info";

    /**
     * Constructs a user object with its unique identifier, the email address.
     */
    UserInfo(final String email) {
        this.email = email;
    }

    /**
     * Returns the email identifier.
     */
    String getEmail() {
        return email;
    }

    /**
     * Changes the name attribute.
     */
    void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the name attribute.
     */
    String getName() {
        return name;
    }

    /**
     * Changes the gender attribute.
     */
    void setGender(final char gender) {
        this.gender = gender;
    }

    /**
     * Returns the gender attribute.
     */
    char getGender() {
        return gender;
    }

    /**
     * Changes the address attribute.
     */
    void setAddress(final String address) {
        this.address = address;
    }

    /**
     * Returns the address attribute.
     */
    String getAddress() {
        return address;
    }

    /**
     * Changes the phone attribute.
     */
    void setPhone(final String phone) {
        this.phone = phone;
    }

    /**
     * Returns the phone attribute.
     */
    String getPhone() {
        return phone;
    }

    /**
     * Returns a Key that can be used to write or read the UserInfo.
     */
    Key getStoreKey() {
        return Key.createKey(Arrays.asList(LoadCookbookData.USER_OBJECT_TYPE,
                                           email),
                             INFO_PROPERTY_NAME);
    }

    /**
     * Serializes user info attributes into the byte array of a Value.
     */
    Value getStoreValue() {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);

        try {
            writeString(dos, name);
            dos.writeChar(gender);
            writeString(dos, address);
            writeString(dos, phone);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Value.createValue(baos.toByteArray());
    }

    /**
     * Utility that writes a UTF string and accomodates null values.
     */
    private void writeString(final DataOutput out, final String val)
        throws IOException {

        if (val == null) {
            out.writeBoolean(false);
            return;
        }
        out.writeBoolean(true);
        out.writeUTF(val);
    }

    @Override
    public String toString() {
        return "<UserInfo " + email +
               "\n    name: " + name + ", gender: " + gender + "," +
               "\n    address: " + address + ", phone: " + phone +
               ">";
    }
}
