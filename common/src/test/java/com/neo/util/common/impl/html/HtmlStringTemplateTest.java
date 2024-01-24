package com.neo.util.common.impl.html;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTMX;

class HtmlStringTemplateTest {

    @Test
    void basicSanitationTest() {
        //Arrange
        String badCode = "<script> alert('Test')";

        //Act
        String sanitized = HTMX."<h1> \{badCode} </h1>";

        //Assert
        Assertions.assertFalse(sanitized.contains("alert"));
    }
}
