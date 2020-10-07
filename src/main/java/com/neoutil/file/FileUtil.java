package com.neoutil.file;

import com.neoutil.logging.Logging;
import com.neoutil.logging.Multilogger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FileUtil {

    public static List<File> folderContent(String location) {
        List<File> names = new ArrayList<>();
        try {
            File folder = new File(location);
            File[] folderContent = folder.listFiles();

            names.addAll(Arrays.asList(folderContent));

        }catch (Exception e) {
            Multilogger.getInstance().println(Multilogger.WARN, "There was a problem trying to find the Files", e);
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

    public static String findLastModifiedFile(String location) throws Exception {
        Logging logging = Multilogger.getInstance();

        logging.println(Logging.INFO, "Looking for newest File directory [" + location + "]");
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
                logging.println(Multilogger.DEBUG, "Found directory/file [" + mostRecent.getAbsolutePath() + "]");
            } while (mostRecent.isDirectory());
            logging.println(Multilogger.INFO, "Newest file ["+ mostRecent.getAbsolutePath()+"]");

        }catch (Exception e) {
            logging.println(Multilogger.ERROR,"There was a Problem trying to find the newest file: ");
            throw new Exception(e);
        }

        return mostRecent.getAbsolutePath();
    }
}
