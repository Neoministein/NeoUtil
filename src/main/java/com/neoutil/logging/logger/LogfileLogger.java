package com.neoutil.logging.logger;

import com.neoutil.file.FileWriter;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class LogfileLogger implements Logger, Runnable{

    public static final String BASELOCATION = System.getProperty("user.dir") +"\\logs\\";
    public static final long DEFAULT_SECONDS_BETWEEN_LOG = 10;

    private final int logLevel;
    private final String logLocation;
    private boolean isFileCrated = false;
    private String fileLocation;
    private final long milisecondsBetweenLog;
    private Thread printThread = new Thread(this);

    private String textToPrint = "";



    public LogfileLogger(String logLocation, int logLevel, long miliseconds){
        this.logLocation = logLocation;
        this.logLevel = logLevel;
        this.milisecondsBetweenLog = miliseconds;
    }

    @Override
    public void print(String text) {
        textToPrint += text;
        if(!printThread.isAlive()){
            printThread.setDaemon(true);
            printThread.start();
        }
    }

    public static String createDefaultDebugFile(String location) {

        String logFileLocation = location +
                (new SimpleDateFormat("yyyy.MM.dd - HH.mm.ss").format(
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
    public String getLoglocation() {
        return logLocation;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep( milisecondsBetweenLog );
            } catch (InterruptedException e) {
            }
            if (!isFileCrated) {
                fileLocation = createDefaultDebugFile( logLocation );
                isFileCrated = true;
            }
            FileWriter.writeToFile( fileLocation, textToPrint );
            textToPrint = "";
        }
    }
}
