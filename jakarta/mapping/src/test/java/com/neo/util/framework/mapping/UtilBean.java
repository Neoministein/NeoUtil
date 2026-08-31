package com.neo.util.framework.mapping;

import com.neo.util.common.impl.json.JsonUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Named("utilBean")
@ApplicationScoped
public class UtilBean {

    public String reformatDate(String date, String dateFormatIn, String dateFormatOut) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(dateFormatIn);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(dateFormatOut);

            LocalDate parsedDate = LocalDate.parse(date, inputFormatter);
            return parsedDate.format(outputFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public ObjectNode defaultNode() {
        ObjectNode node = JsonUtil.emptyObjectNode();
        node.put("string", "stringValue");
        node.put("number", 1.0);
        node.put("boolean", true);


        return node;
    }

}
