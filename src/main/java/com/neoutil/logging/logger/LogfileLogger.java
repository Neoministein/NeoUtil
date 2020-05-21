package com.neoutil.logging.logger;

import com.neoutil.file.FileWriter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class LogfileLogger implements Logger {

    public static final String BASELOCATION = System.getProperty("user.dir") +"\\logs\\";

    private final int logLevel;
    private final String logLocation;
    private boolean isFileCrated = false;
    private String fileLocation;


    public LogfileLogger(String logLocation, int logLevel){
        this.logLocation = logLocation;
        this.logLevel = logLevel;
    }

    @Override
    public void print(String text) {
        if(!isFileCrated) {
           fileLocation = createDefaultDebugFile(logLocation);
           isFileCrated = true;
        }
        FileWriter.writeToFile(fileLocation, text);
    }

    public static String createDefaultDebugFile(String location) {

        String logFileLocation = location +
                (new SimpleDateFormat("yyyy.MM.dd - HH.mm.ss").format(
                        new Timestamp(System.currentTimeMillis())) +
                        ".txt").replace(":",".");

        FileWriter.createFile(logFileLocation);

        return logFileLocation;

    }

    @Override
    public int getLoglevel() {
        return logLevel;
    }

    @Override
    public boolean isIOLogger() {
        return true;
    }

    @Override
    public String getLoglocation() {
        return logLocation;
    }
}
