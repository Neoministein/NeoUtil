package com.neo.util.file;

import com.neo.util.logging.Logging;
import com.neo.util.logging.Multilogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileUtil {

    private FileUtil() {}

    public static List<File> folderContent(String location) {
        List<File> names = new ArrayList<>();
        try {
            File folder = new File(location);
            File[] folderContent = folder.listFiles();

            names.addAll(Arrays.asList(folderContent));

        }catch (Exception e) {
            Multilogger.getInstance().println(Logging.WARN, "There was a problem trying to find the Files");
        }
        return names;
    }

    public static String findFileInSubFolder(String originalName, File originalFileFolder) {
        File[] list = originalFileFolder.listFiles();
        if(list != null) {
            for (File fil : list) {
                if (fil.isDirectory()) {
                    String location = findFileInSubFolder(originalName, fil);
                    if(location != null) {
                        return location;
                    }
                } else if (originalName.equalsIgnoreCase( fil.getName() )) {
                    return fil.getParentFile().getAbsolutePath();
                }
            }
        }
       return null;
    }

    public static String findLastModifiedFile(String location) throws IOException {
        Logging logging = Multilogger.getInstance();

        logging.println(Logging.DEBUG, "Looking for newest File directory [" + location + "]");
        File mostRecent = new File(location);
        try {
            do {
                Path parentFolder = Paths.get(mostRecent.getPath());

                Optional<File> mostRecentFileOrFolder =
                        Arrays
                                .stream(parentFolder.toFile().listFiles())
                                .max(Comparator.comparingLong(File::lastModified));

                if (mostRecentFileOrFolder.isPresent()) {
                    mostRecent = mostRecentFileOrFolder.get();
                }
                logging.println(Logging.DEBUG, "Found directory/file [" + mostRecent.getAbsolutePath() + "]");
            } while (mostRecent.isDirectory());
            logging.println(Logging.INFO, "Newest file ["+ mostRecent.getAbsolutePath()+"]");

        }catch (Exception e) {
            logging.println(Logging.ERROR,"There was a Problem trying to find the newest file: ");
            throw new IOException(e);
        }

        return mostRecent.getAbsolutePath();
    }

    public static boolean deleteFile(String location){
        Logging logging = Multilogger.getInstance();
        try {
            Files.delete(Paths.get(location));
            logging.println(Logging.DEBUG,"Deleted File at ["+location+"]");
            return true;
        }catch (IOException e) {
            logging.println(Logging.WARN,"There is a problem deleting the File at ["+location+"]",e);
            return false;
        }
    }
}
