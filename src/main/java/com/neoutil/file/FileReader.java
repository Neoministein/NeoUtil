package com.neoutil.file;

import java.io.*;
import java.util.*;
import com.neoutil.logging.*;

public class FileReader {

    public static String[] readFileToArray (String location) {
        try {
            java.io.FileReader fileReader = new java.io.FileReader(System.getProperty("user.dir")+"\\"+location);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<String>();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }

            bufferedReader.close();

            return lines.toArray(new String[0]);
        }catch (IOException ex) {
            Multilogger.getInstance().println(Logging.ERROR,"There was a problem trying to read the file at ["+ location +"]");
            return null;
        }
    }

    public static String readFileToString (String location) {
        StringBuilder buffer = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new java.io.FileReader(location));

            String jsonCode;
            while ((jsonCode = br.readLine()) != null){
                buffer.append(jsonCode);
            }
            br.close();
        } catch (IOException exception) {
            Multilogger.getInstance().println(Logging.ERROR,"There was a problem trying to read the file at ["+ location +"]");
        }
        return buffer.toString();
    }

    public static String readFileToLine(String[] file, int line){
        try {
            return file[line];
        }catch (NullPointerException ex){
            Multilogger.getInstance().println(Logging.WARN,"Can't read file line out of bounds");
        }catch (Exception ex){
            Multilogger.getInstance().println(Logging.WARN,"There was a problem trying to read the file");
        }
        return null;
    }

    public static String readLineToSting(String[] file,int line) {
        Logging loggingHandler = Multilogger.getInstance();
        try {
            String l = readFileToLine(file,line);
            String value = l.substring(l.indexOf(":")+1);
            loggingHandler.println(Logging.DEBUG,"Line ["+ line +"] contains ["+ value +"]");
            return value;
        }catch (Exception ex) {
            loggingHandler.println(Logging.WARN,"There was no value found on line ["+ line +"]");
            return null;
        }
    }

    public static int readLineToInt(String[] file,int line) {
        Logging loggingHandler = Multilogger.getInstance();
        try {
            int value = Integer.parseInt(readFileToLine(file,line));
            loggingHandler.println(Logging.DEBUG,"Line ["+ line +"] contains ["+ value+"]");
            return value;
        }catch (Exception ex) {
            loggingHandler.println(Logging.WARN,"No integer value was found on line ["+ line +"]");
            return 0;
        }
    }

    public static boolean readLineToBoolan(String[] file,int line) {
        Logging loggingHandler = Multilogger.getInstance();
        try {
            boolean value = Boolean.parseBoolean(readLineToSting(file,line).replace(' ', Character.MIN_VALUE));
            loggingHandler.println(Logging.DEBUG,"Line ["+ line +"] contains ["+ value +"]");
            return value;
        }catch (Exception ex) {
            Multilogger.getInstance().println(Logging.WARN,"There was a wrong value found on line ["+ line +"]");
            return false;
        }
    }
}
