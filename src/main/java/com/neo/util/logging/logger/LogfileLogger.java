package com.neo.util.logging.logger;

import com.neo.util.file.FileWriter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class LogfileLogger implements Logger, Runnable{

    public static final long SECONDS_BETWEEN_LOG = 1;
    private static final String FILE_NAME_FORMAT = "yyyy.MM.dd - HH.mm.ss";

    private final int logLevel;
    private final long ms;
    private final String fileLocation;

    private StringBuilder textToPrint = new StringBuilder();

    public LogfileLogger(String logLocation, int logLevel, long ms) {
        this.ms = ms;
        this.logLevel = logLevel;

        this.fileLocation = createDefaultDebugFile((System.getProperty("user.dir") +"\\" + logLocation));
        Thread printThread = new Thread(this);
        printThread.setDaemon(true);
        printThread.start();
    }

    @Override
    public void print(StringBuilder text) {
        textToPrint.append(text);
    }

    public String createDefaultDebugFile(String location) {
        String logFileLocation = location +
                (new SimpleDateFormat(FILE_NAME_FORMAT).format(
                        new Timestamp(System.currentTimeMillis())) +
                        ".txt").replace(":",".");

        FileWriter.createFileNoLog(logFileLocation);

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
    public String getLogLocation() {
        return fileLocation;
    }

    @Override
    public void run() {
        boolean loggerIsActive = true;
        while (loggerIsActive) {
            try {
                Thread.sleep(ms);

                FileWriter.appendToFile(fileLocation, textToPrint.toString());
                textToPrint = new StringBuilder();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                loggerIsActive = false;
            }

        }
    }
}
