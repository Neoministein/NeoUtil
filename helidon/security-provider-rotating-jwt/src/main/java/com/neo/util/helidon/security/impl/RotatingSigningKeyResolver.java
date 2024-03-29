package com.neo.util.helidon.security.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.exception.ConfigurationException;
import com.neo.util.common.impl.exception.ValidationException;
import com.neo.util.common.impl.exception.ExceptionDetails;
import com.neo.util.helidon.security.impl.key.JWTKey;
import com.neo.util.helidon.security.impl.key.JWTPublicKey;
import com.neo.util.common.impl.KeyUtils;
import com.neo.util.common.impl.retry.RetryHttpExecutor;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.common.impl.exception.CommonRuntimeException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class RotatingSigningKeyResolver extends SigningKeyResolverAdapter {

    private static final Logger LOGGER =  LoggerFactory.getLogger(RotatingSigningKeyResolver.class);

    protected static final ExceptionDetails EX_INVALID_URL = new ExceptionDetails(
            "auth/jwt/invalid-public-key-url", "Invalid public key url {0}", true
    );
    protected static final ExceptionDetails EX_CANNOT_REACH_PUBLIC_KEY = new ExceptionDetails(
            "auth/jwt/cannot-reach-public-key-endpoint", "Cannot reach public key endpoint", true
    );
    protected static final ExceptionDetails EX_INVALID_PUBLIC_KEY = new ExceptionDetails(
            "auth/jwt/invalid-public-key","Cannot parse json result from PublicKey Endpoint", true
    );

    protected static final int TEN_SECONDS = 10 * 1000;
    protected long lastUpdate = 0L;

    protected final HttpRequest publicKeyEndpoint;
    protected Map<String, JWTKey> keyMap = new HashMap<>();
    protected RetryHttpExecutor lazyHttpExecutor;

    public RotatingSigningKeyResolver(String publicKeyEndpoint, boolean isSecurityService) {
        this(publicKeyEndpoint, isSecurityService, new RetryHttpExecutor());
    }


    protected RotatingSigningKeyResolver(String publicKeyEndpoint, boolean isSecurityService, RetryHttpExecutor lazyHttpExecutor) {
        this.lazyHttpExecutor = lazyHttpExecutor;
        try {
            this.publicKeyEndpoint = HttpRequest.newBuilder().uri(new URI(publicKeyEndpoint)).GET().build();
        } catch (URISyntaxException e) {
            throw new ConfigurationException(EX_INVALID_URL, publicKeyEndpoint);
        }
        if (!isSecurityService) {
            updateCache();
        }
    }

    @Override
    public Key resolveSigningKey(JwsHeader jwsHeader, Claims claims) {
        LOGGER.trace("Resolving public key");
        String kid = jwsHeader.getKeyId();
        if (kid == null) {
            throw new MalformedJwtException("Kid header parameter is missing");
        }

        JWTKey jwtPublicKey =  keyMap.get(kid);

        if (jwtPublicKey != null) {
            LOGGER.trace("Public key found [{}]", kid);
            return jwtPublicKey.getKey();
        }

        LOGGER.trace("Cannot find public key [{}] trying to update cache", kid);
        if (updateCache()) {
            jwtPublicKey =  keyMap.get(kid);

            if (jwtPublicKey != null) {
                return jwtPublicKey.getKey();
            }
        } else {
            LOGGER.trace("Last cache refresh call has been less then {} seconds ago refresh skipped", TEN_SECONDS/1000);
        }
        throw new SignatureException("Cannot find matching public key for kid");
    }

    private synchronized boolean updateCache() {
        if (lastUpdate < System.currentTimeMillis() - TEN_SECONDS) {
            return checkForNewKey();
        }
        return false;
    }

    protected boolean checkForNewKey() {
        boolean hasChanged;
        try {
            LOGGER.trace("Calling public key endpoint [{}]", publicKeyEndpoint.uri());
            String response = lazyHttpExecutor.execute(HttpClient.newHttpClient(), publicKeyEndpoint, 5);

            Map<String, JWTKey> newMap = parseEndpointResult(response);
            lastUpdate = System.currentTimeMillis();
            LOGGER.debug("Updated last public key refresh call time stamp to [{}]", lastUpdate);
            hasChanged = !newMap.keySet().equals(keyMap.keySet());
            if (hasChanged) {
                LOGGER.debug("Updating cached public keys removing keys {} adding keys {}",
                        keyMap.keySet().removeAll(newMap.keySet()),
                        newMap.keySet().removeAll(keyMap.keySet()));
                keyMap = newMap;
            } else {
                LOGGER.trace("No new keys have been found");
            }

            return hasChanged;
        } catch (CommonRuntimeException e) {
            throw new CommonRuntimeException(EX_CANNOT_REACH_PUBLIC_KEY);
        }
    }

    protected Map<String, JWTKey> parseEndpointResult(String resultString) {
        try {
            JsonNode result = JsonUtil.fromJson(resultString);
            Map<String, JWTKey> newMap = new HashMap<>();
            JsonNode data = result.get("data");
            if (!data.isNull()) {
                for (int i = 0; i < data.size(); i++) {
                    JsonNode jwtPublicKeyObject = data.get(i);


                    JWTKey jwtPublicKey = new JWTPublicKey(
                            jwtPublicKeyObject.get("kid").asText(),
                            KeyUtils.parseRSAPublicKey(jwtPublicKeyObject.get("key").asText()),
                            new Date(jwtPublicKeyObject.get("exp").asLong())
                    );

                    newMap.put(jwtPublicKey.getId(), jwtPublicKey);
                }
            }
            LOGGER.trace("Received public kid {}", newMap.keySet());
            return newMap;
        } catch (ValidationException ex) {
            LOGGER.error("Cannot parse json result from PublicKey Endpoint");
            throw new CommonRuntimeException(EX_INVALID_PUBLIC_KEY);
        }
    }
}
