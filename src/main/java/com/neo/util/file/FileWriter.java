package com.neo.util.file;

import com.neo.util.logging.Logging;
import com.neo.util.logging.Multilogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileWriter {

    private FileWriter() {}

    /*
     * You need to close the writer after you are finished!
     */
    public static BufferedWriter getWriter(String location,boolean append) throws IOException {
        return new BufferedWriter(new java.io.FileWriter(location, append));
    }

    public static void fileNotFound(String location, Exception ex) {
        Multilogger.getInstance().println(Logging.ERROR,"There creating a FileWriter at ["+location+"]" ,ex, true);
    }

    public static void appendToFile(String location, String fileContent) {
        try (BufferedWriter writer = new BufferedWriter(getWriter(location,true))) {
            writer.write(fileContent);
        }catch (IOException ex) {
            fileNotFound(location,ex);
        }
    }

    public static void writeToFile(String location, String fileContent) {
        try (BufferedWriter writer = new BufferedWriter(getWriter(location,false))) {
            writer.write(fileContent);
        }catch (IOException ex) {
            fileNotFound(location, ex);
        }
    }

    public static boolean createFileNoLog(String location){
        Logging logging = Multilogger.getInstance();
        try {
            return new File(location).createNewFile();
        } catch (IOException ex) {
            logging.println(Logging.FATAL,"There is a problem creating a the File at ["+location+"]",ex);
        }
        return false;
    }

    public static void createFile(String location){
        Logging logging = Multilogger.getInstance();
        try {
            Files.createFile(Paths.get(location));
        } catch (IOException ex) {
            logging.println(Logging.FATAL,"There is a problem creating a the File at ["+location+"]",ex);
        }
        logging.println(Logging.DEBUG, "Created new file at [" + location + "]" );
    }
}
