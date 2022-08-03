package com.neo.util.framework.api.connection;

public record RequestContext(String httpMethod, String uri) {

    @Override
    public String toString() {
        return httpMethod + " " + uri;
    }
}
