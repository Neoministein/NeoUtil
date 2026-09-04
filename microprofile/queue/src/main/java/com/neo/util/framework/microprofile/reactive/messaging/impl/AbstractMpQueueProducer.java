package com.neo.util.framework.microprofile.reactive.messaging.impl;

import com.neo.util.framework.api.queue.QueueProducer;
import com.neo.util.framework.microprofile.reactive.messaging.api.ReactiveMessageTransformer;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.FlowAdapters;

import java.util.concurrent.SubmissionPublisher;

public abstract class AbstractMpQueueProducer implements QueueProducer {

    protected final String queueName;
    protected final SubmissionPublisher<String> emitter = new SubmissionPublisher<>();

    protected final ReactiveMessageTransformer reactiveMessageTransformer;


    protected AbstractMpQueueProducer(String queueName, ReactiveMessageTransformer reactiveMessageTransformer) {
        this.queueName = queueName;
        this.reactiveMessageTransformer = reactiveMessageTransformer;
    }

    @Override
    public void addToQueue(String msg) {
        emitter.submit(msg);
    }

    protected PublisherBuilder<Object> createPublisher() {
        return ReactiveStreams.fromPublisher(FlowAdapters.toPublisher(emitter)).map(reactiveMessageTransformer.getMessageTransformer(queueName));
    }

    @Override
    public String getQueueName() {
        return queueName;
    }
}
