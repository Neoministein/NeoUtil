package com.neo.util.logging;

import com.neo.util.logging.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.*;

public class Multilogger implements Logging{

    private static final int START_OF_STRING = 0;
    private static final String SIMPLE_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT);

    private LevelToString levelToString = Logging.defaultToString;

    private static final Multilogger instance = new Multilogger();
    private final List<Logger> loggers = new ArrayList<>();

    private Multilogger() {}

    public static Multilogger getInstance() {
        return  instance;
    }

    @Override
    public void println(int loggingLevel, String text){
        log(loggingLevel, new StringBuilder(text)
                .append(System.lineSeparator()));
    }

    @Override
    public void println(int loggingLevel, String text, Exception exception){
        log(loggingLevel, new StringBuilder(text)
                .append(Logging.stackTraceToString(exception))
                .append(System.lineSeparator()));
     }

    @Override
    public void println(int loggingLevel, String text, boolean noIO) {
        StringBuilder toPrint = new StringBuilder(text)
                .append(System.lineSeparator());

        if (noIO) {
            logNoIO(loggingLevel, toPrint);
        } else {
            log(loggingLevel, toPrint);
        }
    }

    @Override
    public void println(int loggingLevel, String text, Exception exception, boolean noIO) {
        StringBuilder toPrint = new StringBuilder(text)
                .append(Logging.stackTraceToString(exception))
                .append(System.lineSeparator());

        if(noIO){
            logNoIO(loggingLevel, toPrint);
        } else {
            log(loggingLevel, toPrint);
        }
    }

    private void logNoIO(int loggingLevel, StringBuilder textToPrint) {
        textToPrint.insert(START_OF_STRING, generatePreText(loggingLevel));

        for (Logger logger : loggers) {
            if (!logger.isIOLogger() && loggingLevel <= logger.getLoglevel()) {
                logger.print(textToPrint);
            }
        }
    }

    private void log(int loggingLevel, StringBuilder textToPrint){
        textToPrint.insert(START_OF_STRING, generatePreText(loggingLevel));
        for (Logger logger : loggers){
            if (loggingLevel <= logger.getLoglevel()){
                logger.print(textToPrint);
            }
        }
    }

    private StringBuilder generatePreText(int loggingLevel){
        return new StringBuilder()
                .append("[")
                .append(dateTimeFormat.format(new Date()))
                .append("]")
                .append(levelToString.levelToString(loggingLevel));
    }

    public void addLogger(Logger logger){
        loggers.add(logger);
        println(Logging.DEBUG,"new Log location at ["+logger.getLogLocation()+"]");
    }

    public void setLevelToString(LevelToString levelToString) {
        this.levelToString = levelToString;
    }

    public void clearLogger(){
        loggers.clear();
    }
}
