package com.neo.util.framework.api.connection;

public sealed interface RequestContext permits RequestContext.Http, RequestContext.Queue {

    String toString();

    record Http(String httpMethod, String uri) implements RequestContext {
        @Override
        public String toString() {
            return httpMethod + " " + uri;
        }
    }

    record Queue(String queueName) implements RequestContext {
        @Override
        public String toString() {
            return queueName;
        }
    }
}
