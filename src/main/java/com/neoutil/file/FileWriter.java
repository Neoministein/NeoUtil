package com.neoutil.file;

import com.neoutil.logging.Logging;
import com.neoutil.logging.Multilogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class FileWriter {
    public static void writeToFile(String location, String fileContent) {
        Logging loggingHandler = Multilogger.getInstance();
        try {
            BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(location,true));
            writer.write(fileContent);
            writer.close();
        }catch (IOException ex) {
            loggingHandler.printlnNoIO(Multilogger.ERROR,"There was problem trying to write toa a file at ["+location+"]",ex);
        }
    }
    public static void createFile(String location){
        Logging loggingHandler = Multilogger.getInstance();
        try {
            new File(location).createNewFile();

        } catch (IOException ex) {
            loggingHandler.printlnNoIO(Multilogger.FATAL,"There is a problem creating a the File at Path ["+location+"]",ex);
        }
    }
}
