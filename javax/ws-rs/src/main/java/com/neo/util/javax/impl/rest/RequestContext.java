package com.neo.util.javax.impl.rest;

public class RequestContext {

    private final HttpMethod httpMethod;
    private final String classURI;
    private final String methodURI;

    public RequestContext(HttpMethod httpMethod, String classURI, String methodURI) {
        this.httpMethod = httpMethod;
        this.classURI = classURI;
        this.methodURI = methodURI;
    }

    @Override
    public String toString() {
        return httpMethod + " " + classURI + methodURI;
    }
}
