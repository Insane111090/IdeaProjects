/*-
 * See the file LICENSE for redistribution information.
 *
 * Copyright (c) 2010, 2013 Oracle and/or its affiliates.  All rights reserved.
 *
 */

package schema;

import java.io.InputStream;
import java.io.IOException;
import java.util.Map;

import oracle.kv.avro.AvroCatalog;
import oracle.kv.avro.GenericAvroBinding;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Parser;

/**
 * Contains the Avro schemas and bindings used to perform serialization of the
 * Value in a kv pair.  The Avro schemas are stored as resource files, and
 * would normally be part of the application jar file.
 * <p>
 * In this example, GenericAvroBinding is used.  Other types of bindings are
 * also available. See the {@link oracle.kv.avro} package and the Oracle NoSQL
 * Database Getting Started Guide for more information.
 */
class Bindings {
    private static final String GENDER_SCHEMA = "schema.Gender";
    private static final String USER_INFO_RESOURCE = "user-info.avsc";
    private static final String USER_INFO_SCHEMA = "schema.UserInfo";
    private static final String USER_IMAGE_RESOURCE = "user-image.avsc";
    private static final String USER_IMAGE_SCHEMA = "schema.UserImage";
    private static final String LOGIN_SESSION_RESOURCE = "login-session.avsc";
    private static final String LOGIN_SESSION_SCHEMA = "schema.LoginSession";
    private static final String LOGIN_SUMMARY_RESOURCE = "login-summary.avsc";
    private static final String LOGIN_SUMMARY_SCHEMA = "schema.LoginSummary";

    private final Schema genderSchema;
    private final Schema userInfoSchema;
    private final Schema userImageSchema;
    private final Schema loginSessionSchema;
    private final Schema loginSummarySchema;
    private final GenericAvroBinding userInfoBinding;
    private final GenericAvroBinding userImageBinding;
    private final GenericAvroBinding loginSessionBinding;
    private final GenericAvroBinding loginSummaryBinding;

    Bindings(AvroCatalog avroCatalog) {

        /* Parse all schema resource files. */
        final Parser parser = new Parser();
        parseResource(parser, USER_INFO_RESOURCE);
        parseResource(parser, USER_IMAGE_RESOURCE);
        parseResource(parser, LOGIN_SESSION_RESOURCE);
        parseResource(parser, LOGIN_SUMMARY_RESOURCE);

        /* Get schemas by name from parser. */
        final Map<String, Schema> types = parser.getTypes();
        genderSchema = types.get(GENDER_SCHEMA);
        userInfoSchema = types.get(USER_INFO_SCHEMA);
        userImageSchema = types.get(USER_IMAGE_SCHEMA);
        loginSessionSchema = types.get(LOGIN_SESSION_SCHEMA);
        loginSummarySchema = types.get(LOGIN_SUMMARY_SCHEMA);

        /* Create bindings from schemas. */
        userInfoBinding = avroCatalog.getGenericBinding(userInfoSchema);
        userImageBinding = avroCatalog.getGenericBinding(userImageSchema);
        loginSessionBinding =
            avroCatalog.getGenericBinding(loginSessionSchema);
        loginSummaryBinding =
            avroCatalog.getGenericBinding(loginSummarySchema);
    }

    private void parseResource(Parser parser, String resourceName) {
        final InputStream in = getClass().getResourceAsStream(resourceName);
        try {
            try {
                parser.parse(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error parsing schema", e);
        }
    }

    Schema getGenderSchema() {
        return genderSchema;
    }

    Schema getUserInfoSchema() {
        return userInfoSchema;
    }

    Schema getUserImageSchema() {
        return userImageSchema;
    }

    Schema getLoginSessionSchema() {
        return loginSessionSchema;
    }

    Schema getLoginSummarySchema() {
        return loginSummarySchema;
    }

    GenericAvroBinding getUserInfoBinding() {
        return userInfoBinding;
    }

    GenericAvroBinding getUserImageBinding() {
        return userImageBinding;
    }

    GenericAvroBinding getLoginSessionBinding() {
        return loginSessionBinding;
    }

    GenericAvroBinding getLoginSummaryBinding() {
        return loginSummaryBinding;
    }
}
