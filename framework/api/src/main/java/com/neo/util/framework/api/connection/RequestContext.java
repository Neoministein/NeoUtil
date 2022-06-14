package com.neo.util.framework.api.connection;

public class RequestContext {

    private final String httpMethod;
    private final String uri;

    public RequestContext(String httpMethod, String uri) {
        this.httpMethod = httpMethod;
        this.uri = uri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return httpMethod + " " + uri;
    }
}
