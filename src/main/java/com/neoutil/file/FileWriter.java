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

    public static void createFileNoLog(String location){
        Logging loggingHandler = Multilogger.getInstance();
        try {
            new File(location).createNewFile();

        } catch (IOException ex) {
            loggingHandler.println(Multilogger.FATAL,"There is a problem creating a the File at ["+location+"]",ex);
        }
    }

    public static void createFile(String location){
        Logging loggingHandler = Multilogger.getInstance();
        try {
            new File(location).createNewFile();

        } catch (IOException ex) {
            loggingHandler.println(Multilogger.FATAL,"There is a problem creating a the File at ["+location+"]",ex);
        }
        loggingHandler.println( Multilogger.DEBUG, "Created new file at [" + location + "]" );

    }

    public static void deleteFile(String location){
        Logging loggingHandler = Multilogger.getInstance();

        File file = new File(location);
        if(file.delete()) {
            loggingHandler.println(Multilogger.DEBUG,"Deleted File at ["+location+"]");
        } else {
            loggingHandler.println(Multilogger.WARN,"There is a problem deleting the File at ["+location+"]");
        }
    }
}
