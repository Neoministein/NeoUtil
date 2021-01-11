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

    void println(int loggingLevel, String text, boolean noIO);

    void println(int loggingLevel, String text, Exception exception, boolean noIO);

    static StringBuilder stackTraceToString(Exception exception) {
        StringBuilder stackTrace = new StringBuilder();
        stackTrace.append("\n")
                .append(exception.getStackTrace()[0].getClassName())
                .append(": ")
                .append(exception.getMessage());
        for(StackTraceElement stackTraceElement: exception.getStackTrace()) {

            stackTrace.append("\n" + "    at ")
                    .append(stackTraceElement.getClassName())
                    .append(".")
                    .append(stackTraceElement.getMethodName())
                    .append("(")
                    .append(stackTraceElement.getMethodName())
                    .append(".")
                    .append(stackTraceElement.getLineNumber())
                    .append(")");

        }
        return stackTrace;
    }

    void setLevelToString(LevelToString levelToString);
}
