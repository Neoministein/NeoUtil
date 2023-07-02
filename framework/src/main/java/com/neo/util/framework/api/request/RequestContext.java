package com.neo.util.framework.api.request;

public interface RequestContext {

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

    record Scheduler(String timerName) implements RequestContext {
        @Override
        public String toString() {
            return timerName;
        }
    }
}
