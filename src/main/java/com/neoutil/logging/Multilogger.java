package com.neoutil.logging;

import com.neoutil.logging.logger.LogfileLogger;
import com.neoutil.logging.logger.Logger;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Multilogger implements Logging {

    private String stringToLog = "";
    private LevelToString levelToString = Logging.defaultToString;

    private static Multilogger instance = new Multilogger();
    private List<Logger> loggers = new ArrayList<>();

    private Multilogger() {
        addLogger(new LogfileLogger(LogfileLogger.BASELOCATION,Integer.MAX_VALUE,LogfileLogger.DEFAULT_SECONDS_BETWEEN_LOG));
    }
    public static Multilogger getInstance() {
        return  instance;
    }

    @Override
    public void println(int loggingLevel, String text){
        print(loggingLevel,text+"\n");
    }

    @Override
    public void println(int loggingLevel, String text, Exception exception){
        print(loggingLevel,text+Logging.stackTraceToString(exception)+"\n");
    }

    @Override
    public void print(int loggingLevel,String text){
        if(hasNewLine(text)){
            log(loggingLevel);
            resetLoggingText();
        }
    }

    @Override
    public void printlnNoIO(int loggingLevel, String text) {
        printNoIO(loggingLevel,text+"\n");
    }

    @Override
    public void printlnNoIO(int loggingLevel, String text, Exception exception) {
        printNoIO(loggingLevel,text+Logging.stackTraceToString(exception)+"\n");
    }

    @Override
    public void printNoIO(int loggingLevel,String text) {
        for (Logger logger : loggers) {
            if (!logger.isIOLogger()) {
                if (loggingLevel <= logger.getLoglevel()) {
                    logger.print(text);
                }
            }
        }
    }

    @Override
    public void printlnToLevel(int loggingLevel, String text) {
        printToLevel(loggingLevel,text+"\n");
    }

    @Override
    public void printlnToLevel(int loggingLevel, String text, Exception exception) {
        printToLevel(loggingLevel,text+Logging.stackTraceToString(exception)+"\n");
    }

    @Override
    public void printToLevel(int loggingLevel, String text) {
        for (Logger logger : loggers){
            if (loggingLevel == logger.getLoglevel()){
                logger.print(text);
            }
        }
    }

    private boolean hasNewLine(String textToPrint) {
        stringToLog += textToPrint;
        return stringToLog.contains("\n") || stringToLog.contains("\r");
    }

    private void resetLoggingText(){
        stringToLog = "";
    }

    private void log(int loggingLevel){
        String textToPrint = generatePreText(loggingLevel);

        for (Logger logger : loggers){
            if (loggingLevel <= logger.getLoglevel()){
                logger.print(textToPrint);
            }
        }
    }

    private String generatePreText(int loggingLevel){
        return "["+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(
                new Timestamp(System.currentTimeMillis()))+"]" +
                levelToString.run(loggingLevel)+
                stringToLog;
    }

    public void addLogger(Logger logger){
        loggers.add(logger);
        println(Multilogger.DEBUG,"new Log location at ["+logger.getLoglocation()+"]");
    }

    public void setLevelToString(LevelToString levelToString) {
        this.levelToString = levelToString;
    }

    public void clearLogger(){
        loggers.clear();
    }
}
