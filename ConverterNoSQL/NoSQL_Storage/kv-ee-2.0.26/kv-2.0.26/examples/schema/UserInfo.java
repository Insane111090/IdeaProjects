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
import org.apache.avro.generic.GenericEnumSymbol;
import org.apache.avro.generic.GenericRecord;

/**
 * Holds the collection of user attributes that is stored as the
 * {@code Value} for the "/user/EMAIL/-/info" {@code Key}.
 * Illustrates the use of a multi-attribute Key/Value pair.
 * 
 * <p>Memory and disk overhead is reduced when storing multiple
 * attributes in a single {@code Value}, compared to storing each
 * individual attribute as a separate Key/Value pair.  The
 * multi-attribute approach is appropriate when more than one
 * attribute is often accessed at a time, and the attributes are
 * generally small.  When a large attribute is not typically accessed
 * along with other attributes, it is generally better to store it as
 * a separate Key/Value pair; for example, see the {@link UserImage}
 * class.</p>
 *
 * <p>This class implements serialization and deserialization of its
 * attributes as a {@code Value} object. A {@code Value} object is
 * used to access a Key/Value pair, and is really just a wrapper for a
 * byte array.</p>
 *
 * <p>To translate attributes to and from the byte array, the standard
 * Java {@code DataOutputStream} and {@code DataInputStream} classes
 * are used.  It is important to minimize the size of the byte array,
 * and {@code DataOutput} and {@code DataInput} provide low-level
 * methods for optimizing the storage size.  Of course, many other
 * serialization techniques are available, and the use of {@code
 * DataOutputStream} and {@code DataInputStream} is only used here as
 * one example.</p>
 *
 * <p>Standard Java serialization is not recommended because of the
 * large per-object overhead that would be repeated in every {@code
 * Value} containing objects of the same class.  Even when using the
 * {@code Externalizable} approach, the class name is stored with each
 * object.  This redundancy is not appropriate for large scale data
 * storage.</p>
 *
 * <p>In a real application, provisions should be made for schema versioning.
 * Schema versioning is beyond the scope of this example and the serialization
 * format used here contains no version information.</p>
 */
@SuppressWarnings("javadoc")
class UserInfo {

    /*
     * The email address is a unique identifier and is used to construct
     * the Key's major path.
     */
    private final String email;

    /* Persistent fields stored in the Value. */
    private String name;
    private Gender gender;
    private String address;
    private String phone;

    /**
     * Constructs a user object with its unique identifier, the email address.
     */
    UserInfo(String email) {
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
    void setName(String name) {
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
    void setGender(Gender gender) {
        this.gender = gender;
    }

    /**
     * Returns the gender attribute.
     */
    Gender getGender() {
        return gender;
    }

    /**
     * Changes the address attribute.
     */
    void setAddress(String address) {
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
    void setPhone(String phone) {
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
        return KeyDefinition.makeUserInfoKey(email);
    }

    /**
     * Serializes user info attributes into the byte array of a Value.
     */
    Value getStoreValue(Bindings bindings) {
        final GenericRecord rec =
            new GenericData.Record(bindings.getUserInfoSchema());
        rec.put("name", name);
        final GenericEnumSymbol genderSymbol =
            new GenericData.EnumSymbol(bindings.getGenderSchema(),
                                       gender.toString());
        rec.put("gender", genderSymbol);
        rec.put("address", address);
        rec.put("phone", phone);
        return bindings.getUserInfoBinding().toValue(rec);
    }

    /**
     * Deserializes user info attributes from the byte array of a Value.
     */
    void setStoreValue(Bindings bindings, Value value) {
        final GenericRecord rec =
            bindings.getUserInfoBinding().toObject(value);
        name = rec.get("name").toString();
        final GenericEnumSymbol genderSymbol =
            (GenericEnumSymbol) rec.get("gender");
        gender = Enum.valueOf(Gender.class, genderSymbol.toString());
        address = rec.get("address").toString();
        phone = rec.get("phone").toString();
    }

    @Override
    public String toString() {
        return "<UserInfo " + email +
               "\n    name: " + name + ", gender: " + gender + "," +
               "\n    address: " + address + ", phone: " + phone +
               ">";
    }
}
