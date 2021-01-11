package com.neo.util.logging.logger;

import com.neo.util.logging.Logging;
import com.neo.util.logging.Multilogger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogFileLoggerSpeedTest {

    private static final Logging logging = Multilogger.getInstance();

    private static long timeStart = 0;

    @BeforeClass
    public static void loggerInitialization() {
        Multilogger multilogger = Multilogger.getInstance();

        Logger file = new LogfileLogger("logs/",10,LogfileLogger.SECONDS_BETWEEN_LOG);

        multilogger.addLogger(file);
        timeStart = System.nanoTime();
    }

    @AfterClass
    public static void timeTaken() {
        long timeSpent = System.nanoTime() - timeStart;
        logging.println(Logging.INFO,"Time taken: " + timeSpent/1000000 + "|" + timeSpent);
    }

    @Test
    public void logToFile(){
        int i = 0;
        for (; i < 10000;i++){
            logging.println(Logging.INFO,"Logging: "+i);
        }
        assertEquals(10000, i);
    }
}
