package com.neo.util.file;

import java.net.URL;

public class ResourceGatherer {

    private ResourceGatherer(){}

    public static URL getResourceURL(String fileName) {
        return  ResourceGatherer.class.getClassLoader().getResource(fileName);
    }

    public static String getResourceLocation(String fileName) {
        return getResourceURL(fileName).getPath();
    }
}
