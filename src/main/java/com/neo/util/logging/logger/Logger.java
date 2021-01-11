package com.neo.util.logging.logger;

public interface Logger {

    void print(StringBuilder text);

    int getLoglevel();

    boolean isIOLogger();

    String getLogLocation();
}
