package com.neo.util.framework.mapping;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Named("utilBean")
@ApplicationScoped
public class UtilBean {

    public static String reformatDate(String date, String dateFormatIn, String dateFormatOut) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(dateFormatIn);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(dateFormatOut);

            LocalDate parsedDate = LocalDate.parse(date, inputFormatter);
            return parsedDate.format(outputFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
