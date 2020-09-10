package com.neoutil.file.json;

import com.neoutil.file.FileReader;
import org.json.JSONArray;
import org.json.JSONObject;

public class JSONReader {
    public static JSONArray readToJSONArray(String location) {
        return new JSONArray(FileReader.readFileToString(location));
    }

    public static JSONObject readToJSONObject(String location) {
        return new JSONObject(FileReader.readFileToString(location));
    }
}