package com.neo.util.common.impl.reflection;

import com.neo.util.common.impl.StringUtils;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Rendex {

    private Set<String> resourceIndex;

    public Rendex(InputStream is) {
        resourceIndex = new HashSet<>(StringUtils.readLines(is));
    }

    public Rendex() {
        resourceIndex = new HashSet<>();
    }

    public void addFile(String path) {
        resourceIndex.add(path);
    }

    public Set<String> getResources() {
        return resourceIndex;
    }

    public Set<String> getResources(String regex) {
        Pattern pattern = Pattern.compile(regex);
        return resourceIndex.stream().filter(pattern.asPredicate()).collect(Collectors.toSet());
    }


    public Set<String> getResources(Pattern pattern) {
        return resourceIndex.stream().filter(pattern.asPredicate()).collect(Collectors.toSet());
    }
}
