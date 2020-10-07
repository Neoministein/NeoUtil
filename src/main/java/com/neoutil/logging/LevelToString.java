package com.neoutil.logging;

public interface LevelToString {

    String levelToString(int loggingLevel);

    int stringToLevel(String loggingLevel);
}
