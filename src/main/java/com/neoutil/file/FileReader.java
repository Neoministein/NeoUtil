package com.neoutil.file;

import java.io.*;
import java.io.FileWriter;
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

    public static String readLineToSting(String location,int line) {
        Logging loggingHandler = Multilogger.getInstance();
        try {
            String l = readFile(location)[line];
            String value = l.substring(l.indexOf(":")+1);
            loggingHandler.println(Multilogger.DEBUG,"Line ["+ line +"] contains ["+ value +"]");
            return value;
        }catch (Exception ex) {
            loggingHandler.println(Multilogger.WARN,"There was a wrong value found on line ["+ line +"] in file ["+ location+"]");
            return "";
        }
    }

    public static int readLineToInt(String location,int line) {
        Logging loggingHandler = Multilogger.getInstance();
        String l = readFile(location)[line];
        try {
            int value = Integer.parseInt(l.substring(l.indexOf(":")+1));
            loggingHandler.println(Multilogger.DEBUG,"Line ["+ line +"] contains ["+ value+"]");
            return value;
        }catch (Exception ex) {
            loggingHandler.println(Multilogger.WARN,"There was a wrong value found on line ["+ line +"] in file ["+ location+"]");
            return 0;
        }
    }

    public static boolean readLineToBoolan(String location,int line) {
        Logging loggingHandler = Multilogger.getInstance();
        String l = readFile(location)[line];
        try {
            boolean value = Boolean.parseBoolean(l.replaceAll(" ","").substring((l.replaceAll(" ","").indexOf(":")+1)));
            loggingHandler.println(Multilogger.DEBUG,"Line ["+ line +"] contains ["+ value +"]");
            return value;
        }catch (Exception ex) {
            loggingHandler.println(Multilogger.WARN,"There was a wrong value found on line ["+ line +"] in file ["+ location+"]");
            return false;
        }
    }
}
