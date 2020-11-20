package com.neo.util.logging;

public interface Logging {

    int FATAL  = 0;
    int ERROR  = 1;
    int WARN   = 2;
    int NOTICE = 3;
    int INFO   = 4;
    int DEBUG  = 5;

    LevelToString defaultToString = new LevelToString() {
        @Override
        public String levelToString(int loggingLevel) {
            switch (loggingLevel) {
                case FATAL:
                    return "[FATAL]";
                case ERROR:
                    return "[ERROR]";
                case WARN:
                    return "[WARN]";
                case NOTICE:
                    return "[NOTICE]";
                case INFO:
                    return "[INFO]";
                case DEBUG:
                    return "[DEBUG]";
                default:
                    return "[Level:" + loggingLevel + "]";
            }
        }

        @Override
        public int stringToLevel(String loggingLevel) {
            switch (loggingLevel) {
                case "[FATAL]":
                    return FATAL;
                case  "[ERROR]":
                    return ERROR;
                case "[WARN]":
                    return WARN;
                case "[NOTICE]":
                    return NOTICE;
                case "[INFO]":
                    return INFO;
                case "[DEBUG]":
                    return DEBUG;
                default:
                    return 0;
            }
        }
    };

    void println(int loggingLevel, String text);

    void println(int loggingLevel, String text, Exception exception);

    void print(int loggingLevel, String text);

    void printlnNoIO(int loggingLevel, String text);

    void printlnNoIO(int loggingLevel, String text, Exception exception);

    void printNoIO(int loggingLevel,String text);

    void printlnToLevel(int loggingLevel, String text);

    void printlnToLevel(int loggingLevel, String text, Exception exception);

    void printToLevel(int loggingLevel, String text);

    static String stackTraceToString(Exception exception) {
        String stackTrace = ("\n"+exception.getStackTrace()[0].getClassName()+": "+exception.getMessage());
        for(StackTraceElement stackTraceElement: exception.getStackTrace()) {

            stackTrace +=("\n"+"    at "+stackTraceElement.getClassName()+
                    "."+stackTraceElement.getMethodName()+
                    "("+stackTraceElement.getMethodName()+
                    "."+stackTraceElement.getLineNumber()+")");

        }
        return stackTrace;
    }

    void setLevelToString(LevelToString levelToString);
}
