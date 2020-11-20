package com.neo.util.file;

import com.neo.util.logging.Logging;
import com.neo.util.logging.Multilogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileReader {

    /**
     * Returns a io.FileReader which you need to close after you are finished!
     *
     * @param location  location which the reader leads to
     * @return          io.FileReader
     */
    protected static java.io.FileReader getReader(String location) throws FileNotFoundException {
        return new java.io.FileReader(location);
    }

    protected static void fileNotfound(String location) {
        Multilogger.getInstance().println(Logging.ERROR,"Cannot create a FileReader at ["+ location +"]");
    }

    public static List<String> readFileToList(String location){
        List<String> lines = new ArrayList<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(getReader(location));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }

            bufferedReader.close();
        }catch (IOException ex) {
            fileNotfound(location);
        }
        return lines;
    }

    public static String[] readFileToArray (String location) {
        return readFileToList(location).toArray(new String[0]);
    }

    public static String readFileToString (String location) {
        StringBuilder buffer = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(getReader(location));

            String jsonCode;
            while ((jsonCode = br.readLine()) != null){
                buffer.append(jsonCode);
            }
            br.close();
        } catch (IOException exception) {
            fileNotfound(location);
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
}
