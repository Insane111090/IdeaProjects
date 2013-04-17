/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package schema;

import java.nio.ByteBuffer;

import oracle.kv.Key;
import oracle.kv.Value;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

/**
 * Holds the image user attribute that is stored as the {@code Value}
 * for the "/user/EMAIL/-/image" {@code Key}.  Illustrates the use of
 * a single-attribute Key/Value pair.
 *
 * <p>Because the image is expected to be large and is not accessed along
 * with other attributes, it is stored separately from the
 * multi-attribute {@code UserInfo} Key/Value pair.</p>
 */
class UserImage {

    /*
     * The email address is a unique identifier and is used to construct
     * the Key's major path.
     */
    private final String email;

    /* Persistent fields stored in the Value. */
    private byte[] image;

    /**
     * Constructs a user object with its unique identifier, the email address.
     */
    UserImage(String email) {
        this.email = email;
    }

    /**
     * Returns the email identifier.
     */
    String getEmail() {
        return email;
    }

    /**
     * Changes the image bytes.
     */
    void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * Returns the image bytes.
     */
    byte[] getImage() {
        return image;
    }

    /**
     * Returns a Key that can be used to write or read the UserImage.
     */
    Key getStoreKey() {
        return KeyDefinition.makeUserImageKey(email);
    }

    /**
     * Deserializes the image into the byte array of a Value.
     * <p>
     * Note that since there is only one field, a byte array, the Value's byte
     * array could be stored directly.  But using an Avro binding allows for
     * the possibility of adding additional fields in the future.
     */
    Value getStoreValue(Bindings bindings) {
        final GenericRecord rec =
            new GenericData.Record(bindings.getUserImageSchema());
        rec.put("image", ByteBuffer.wrap(image));
        return bindings.getUserImageBinding().toValue(rec);
    }

    /**
     * Deserializes the image from the byte array of a Value.
     * <p>
     * Note that since there is only one field, a byte array, the Value's byte
     * array could be stored directly.  But using an Avro binding allows for
     * the possibility of adding additional fields in the future.
     */
    void setStoreValue(Bindings bindings, Value value) {
        final GenericRecord rec =
            bindings.getUserImageBinding().toObject(value);
        final ByteBuffer buf = (ByteBuffer) rec.get("image");
        image = buf.array();
        assert buf.position() == 0;
        assert image.length == buf.limit();
    }

    @Override
    public String toString() {
        return "<UserImage " + email +
               "\n    imageLength: " + image.length + ">";
    }
}
