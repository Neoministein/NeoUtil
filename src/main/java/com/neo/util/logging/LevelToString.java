package com.neo.util.logging;

public interface LevelToString {

    String levelToString(int loggingLevel);

    int stringToLevel(String loggingLevel);
}
