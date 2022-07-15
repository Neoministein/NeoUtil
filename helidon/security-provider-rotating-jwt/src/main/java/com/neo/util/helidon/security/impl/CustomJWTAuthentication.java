package com.neo.util.helidon.security.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.neo.util.common.impl.KeyUtils;
import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.common.impl.StringUtils;
import com.neo.util.common.impl.exception.InternalJsonException;
import com.neo.util.common.impl.exception.InternalLogicException;
import io.helidon.config.Config;
import io.helidon.security.*;
import io.helidon.security.spi.AuthenticationProvider;
import io.helidon.security.spi.OutboundSecurityProvider;
import io.helidon.security.spi.ProviderConfig;
import io.helidon.security.spi.SynchronousProvider;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.*;
import java.lang.SecurityException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

public class CustomJWTAuthentication extends SynchronousProvider implements AuthenticationProvider, OutboundSecurityProvider {

    private static final Logger LOGGER =  LoggerFactory.getLogger(CustomJWTAuthentication.class);

    protected static final String KEY_ENDPOINT = "publicKeyEndpoint";
    protected static final String IS_SECURITY_SERVICE = "isSecurityService";
    protected static final String IS_INTEGRATION_TEST = "isIntegrationsTest";

    private Map<String, BlockedJWT> blockedJWTToken = new HashMap<>();
    private final JwtParser parser;

    // For unit testing purposes
    protected CustomJWTAuthentication(SigningKeyResolver signingKeyResolver) {
        parser = Jwts.parserBuilder().setSigningKeyResolver(signingKeyResolver).build();
    }

    public CustomJWTAuthentication(Config config) {
        boolean isIntegrationTest = config.get(IS_INTEGRATION_TEST).asBoolean().orElse(false);
        if (!isIntegrationTest) {
            String publicKeyEndpoint = config.get(KEY_ENDPOINT).as(String.class).get();
            Boolean isSecurityService = config.get(IS_SECURITY_SERVICE).asBoolean().orElse(false);
            parser = Jwts.parserBuilder().setSigningKeyResolver(new RotatingSigningKeyResolver(publicKeyEndpoint, isSecurityService))
                    .build();
        } else {
            LOGGER.warn("The CustomJWTAuthentication provider has been started in integration test mode");
            parser = Jwts.parserBuilder().setSigningKey(retrievePrivateKeyFromFile()).build();
        }

        //TODO impl Websocket to get blcoked tockens
    }

    @Override
    public Collection<Class<? extends Annotation>> supportedAnnotations() {
        LOGGER.error("supportedAnnotations attempt");
        return Set.of();
    }

    @Override
    public Collection<String> supportedConfigKeys() {
        LOGGER.error("supportedConfigKeys attempt");
        return Set.of();
    }

    @Override
    public Collection<Class<? extends ProviderConfig>> supportedCustomObjects() {
        LOGGER.error("supportedCustomObjects attempt");
        return Set.of();
    }

    @Override
    public Collection<String> supportedAttributes() {
        LOGGER.error("supportedAttributes attempt");
        return Set.of();
    }

    @Override
    protected AuthenticationResponse syncAuthenticate(ProviderRequest providerRequest) {
        MDC.put("traceId", providerRequest.securityContext().id());
        LOGGER.info("Authentication attempt");
        try {
            String jwtToken = getJWTToken(providerRequest.env().headers());
            Jws<Claims> jws = parser.parseClaimsJws(jwtToken);
            Claims body = jws.getBody();
            checkForBlockedJWT(jws.getHeader(), body);

            String uuid = body.getSubject();
            String name = body.get("username", String.class);
            List<String> roles = body.get("roles", List.class);

            if (uuid == null) {
                throw new MalformedJwtException("The provided token didn't have a uuid attached to it");
            }

            if (name == null) {
                throw new MalformedJwtException("The provided token didn't have a username attached to it");
            }

            if (roles == null) {
                throw new MalformedJwtException("The provided token didn't have a role attribute attached to it");
            }

            Subject.Builder subjectBuilder = Subject.builder().addPrincipal(Principal.builder().id(uuid).name(name).build());
            for (String stringRole: roles) {
                subjectBuilder.addGrant(Role.create(stringRole));
            }

            LOGGER.debug("JWT has been validated user as [{}] with permissions {}", uuid, roles);
            return AuthenticationResponse.success(subjectBuilder.build());
        } catch (SecurityException ex) {
            LOGGER.trace("No JWT Token was provided");
            return AuthenticationResponse.failed(ex.getMessage());
        } catch (ExpiredJwtException ex) {
            LOGGER.trace("Provided token has expired {}", ex.getMessage());
            return AuthenticationResponse.failed("Token has expired");
        } catch (MalformedJwtException ex) {
            LOGGER.trace("Provided token is malformed {}", ex.getMessage());
            return AuthenticationResponse.failed("Not a valid token");
        } catch (SignatureException| UnsupportedJwtException ex)  {
            LOGGER.warn("Token signature {}", ex.getMessage());
            return AuthenticationResponse.failed("Token signature is invalid");
        } catch (InternalLogicException ex) {
            LOGGER.error("An error has occurred {}", ex.getMessage());
            return AuthenticationResponse.failed("Internal authentication error");
        } catch (Exception ex) {
            LOGGER.error("Unknown server error", ex);
            return AuthenticationResponse.failed("Internal authentication error");
        }
    }

    @Override
    protected AuthorizationResponse syncAuthorize(ProviderRequest providerRequest) {
        LOGGER.error("Authorization attempt");
        return AuthorizationResponse.builder().build();
    }

    @Override
    protected OutboundSecurityResponse syncOutbound(ProviderRequest providerRequest,
            SecurityEnvironment outboundEnv, EndpointConfig outboundEndpointConfig) {
        LOGGER.error("Outbound attempt");
        return OutboundSecurityResponse.abstain();
    }

    private String getJWTToken(Map<String, List<String>> headers) {
        LOGGER.trace("Checking header for JWT Token");
        List<String> authorization = headers.get("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            LOGGER.trace("No JWT token in the header checking cookies");
            Map<String, String> cookies = getCookiesFromHeader(headers.get("Cookie"));
            String jwtToken = cookies.get("JWT");
            if (!StringUtils.isEmpty(jwtToken)) {
                return jwtToken;
            }

            throw new SecurityException("No JWT Token found");
        }
        String authorizationHeader = authorization.get(0);
        if (!authorizationHeader.startsWith("Bearer") || authorizationHeader.length() < 7) {
            throw new MalformedJwtException("Authorization header malformed");
        }
        return authorizationHeader.substring(7);
    }

    private Map<String, String> getCookiesFromHeader(List<String> cookieList) {
        if (cookieList == null || cookieList.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> cookieMap = new HashMap<>();
        for (String cookieListEntry: cookieList) {
            for (String cookieContent : cookieListEntry.split(";")) {
                try {
                    String[] cookie = cookieContent.split("=");
                    cookieMap.put(cookie[0], cookie[1]);
                } catch (IndexOutOfBoundsException ex) {
                    LOGGER.debug("Illegal cookie pattern provided");
                }
            }
        }
        return cookieMap;
    }

    protected void checkForBlockedJWT(Header header, Claims claims) {
        BlockedJWT blockedJWT = blockedJWTToken.get(claims.getSubject());
        if (blockedJWT != null && blockedJWT.getInvalidUntil() > claims.getExpiration().getTime()) {
            LOGGER.info("Provided JWT Token has been marked as invalid");
            throw new ExpiredJwtException(header, claims, "JWT Token is marked as blocked");
        }
    }

    protected Key retrievePrivateKeyFromFile() {
        try {
            StringBuilder sb = new StringBuilder();
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("jwt-keys.json");
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            for (String line; (line = reader.readLine()) != null;) {
                sb.append(line);
            }

            JsonNode node = JsonUtil.fromJson(sb.toString());
            return KeyUtils.parseRSAPrivateKey(node.get("private").asText());
        } catch (NullPointerException | IOException | InternalJsonException ex) {
            LOGGER.error("Unable to retrieve private key from resources");
        }
        throw new InternalLogicException();
    }
}