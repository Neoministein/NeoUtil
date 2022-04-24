package com.neo.common.impl.http.verify;

import com.neo.common.api.http.verify.ResponseFormatVerification;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultSuccessResponse implements ResponseFormatVerification {

    private static final Logger LOGGER =  LoggerFactory.getLogger(DefaultSuccessResponse.class);

    @Override
    public boolean verify(String message) {
        try {
            JSONObject jsonMessage = new JSONObject(new JSONTokener(message));
            if (jsonMessage.getInt("Status") == 200) {
                return true;
            }
            LOGGER.trace("Received response isn't ok status code {}", jsonMessage.getInt("Status"));
            if (jsonMessage.has("error")) {
                JSONObject error = jsonMessage.getJSONObject("error");
                LOGGER.trace("Error code {} message {}" , error.getInt("code"), error.getString("message"));
            }
        } catch (JSONException ex) {
            LOGGER.trace("The provided message cannot correctly parsed to json");
        }
        return false;
    }
}