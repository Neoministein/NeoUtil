package com.neo.common.impl.http.verify;

import com.neo.common.api.http.verify.ResponseFormatVerification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DefaultSuccessResponseTest {


    static ResponseFormatVerification subject;

    @BeforeAll
    static void init() {
        subject = new DefaultSuccessResponse();
    }

    @Test
    void validSuccessResponseTest() {
        String jsonResponse = "{\"status\": 200,\"apiVersion\": \"\",\"context\": \"\",\"data\": {}}";

        Assertions.assertTrue(subject.verify(jsonResponse));
    }

    @Test
    void invalidJsonResponseTest() {
        String jsonResponse = "\"status\": 200,\"apiVersion\": \"\",\"context\": \"\",\"data\": {}}";

        Assertions.assertFalse(subject.verify(jsonResponse));
    }

    @Test
    void invalidSuccessResponseTest() {
        String jsonResponse = "\"status\": 404,\"apiVersion\": \"\",\"context\": \"\",\"data\": {}}";

        Assertions.assertFalse(subject.verify(jsonResponse));
    }
}
