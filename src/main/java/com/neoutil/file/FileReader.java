package com.neoutil.file;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import com.neoutil.logging.Logging;
import com.neoutil.logging.Multilogger;

public class FileReader {

    public static String[] readFile (String location) {
        Logging loggingHandler = Multilogger.getInstance();
        try {
            java.io.FileReader fileReader = new java.io.FileReader(System.getProperty("user.dir")+"\\"+location);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<String>();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }

            bufferedReader.close();

            return lines.toArray(new String[lines.size()]);
        }catch (IOException ex) {
            loggingHandler.println(Multilogger.ERROR,"There was a problem trying to read the file at ["+ location +"]");
            return null;
        }
    }
    public static String readFileToLine(String[] file, int line){
        Logging loggingHandler = Multilogger.getInstance();
        try {
            return file[line];
        }catch (NullPointerException ex){
            loggingHandler.println(Multilogger.WARN,"Can't read file line out of bounds");
        }catch (Exception ex){
            loggingHandler.println(Multilogger.WARN,"There was a problem trying to read the file");
        }
        return null;
    }

    public static String readLineToSting(String[] file,int line) {
        Logging loggingHandler = Multilogger.getInstance();
        try {
            String l = readFileToLine(file,line);
            String value = l.substring(l.indexOf(":")+1);
            loggingHandler.println(Multilogger.DEBUG,"Line ["+ line +"] contains ["+ value +"]");
            return value;
        }catch (Exception ex) {
            loggingHandler.println(Multilogger.WARN,"There was no value found on line ["+ line +"]");
            return null;
        }
    }

    public static int readLineToInt(String[] file,int line) {
        Logging loggingHandler = Multilogger.getInstance();
        try {
            int value = Integer.parseInt(readFileToLine(file,line));
            loggingHandler.println(Multilogger.DEBUG,"Line ["+ line +"] contains ["+ value+"]");
            return value;
        }catch (Exception ex) {
            loggingHandler.println(Multilogger.WARN,"No integer value was found on line ["+ line +"]");
            return 0;
        }
    }

    public static boolean readLineToBoolan(String[] file,int line) {
        Logging loggingHandler = Multilogger.getInstance();
        try {
            boolean value = Boolean.parseBoolean(readLineToSting(file,line).replace(' ', Character.MIN_VALUE));
            loggingHandler.println(Multilogger.DEBUG,"Line ["+ line +"] contains ["+ value +"]");
            return value;
        }catch (Exception ex) {
            loggingHandler.println(Multilogger.WARN,"There was a wrong value found on line ["+ line +"]");
            return false;
        }
    }
}
