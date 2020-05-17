package com.neoutil.logging.logger;

public interface Logger {

    void print(String text);

    int getLoglevel();

    boolean isIOLogger();

    String getLoglocation();
}
