package com.neo.util.common.impl.html;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.neo.util.common.impl.html.HtmlStringTemplate.HTML;

class HtmlStringTemplateTest {

    @Test
    void basicSanitationTest() {
        //Arrange
        String badCode = "<script> alert('Test')";

        //Act
        HtmlElement sanitized = HTML."<h1> \{badCode} </h1>";

        //Assert
        Assertions.assertFalse(sanitized.content().contains("alert"));
    }
}
