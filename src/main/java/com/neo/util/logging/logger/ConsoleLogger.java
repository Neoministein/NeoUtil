package com.neo.util.logging.logger;

public class ConsoleLogger implements Logger {

    private final int loglevel;

    public ConsoleLogger(int loglevel){
        this.loglevel = loglevel;
    }

    @Override
    public void print(StringBuilder text) {
        System.out.print(text.toString());
    }

    @Override
    public int getLoglevel() {
        return loglevel;
    }

    @Override
    public boolean isIOLogger() {
        return false;
    }

    @Override
    public String getLogLocation() {
        return "Console";
    }
}
