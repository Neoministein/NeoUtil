package com.neo.javax.api.connection;

public class RequestContext {

    private final String httpMethod;
    private final String uri;

    public RequestContext(String httpMethod, String uri) {
        this.httpMethod = httpMethod;
        this.uri = uri;
    }

    @Override
    public String toString() {
        return httpMethod + " " + uri;
    }
}
