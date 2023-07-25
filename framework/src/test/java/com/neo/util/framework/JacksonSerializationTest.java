package com.neo.util.framework;

import com.neo.util.common.impl.json.JsonUtil;
import com.neo.util.framework.api.persistence.search.QueueableSearchable;
import com.neo.util.framework.api.queue.QueueMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class JacksonSerializationTest {

    @Test
    void queueMessageTest() {
        //Arrange
        String traceId = "aTraceId";
        String initiator = "aCaller";
        String messageType = "testType";
        String payload = "a String Payload";

        QueueMessage expected = new QueueMessage(initiator, traceId,messageType, payload);
        //Act

        String jsonString = JsonUtil.toJson(expected);
        QueueMessage result = JsonUtil.fromJson(jsonString, QueueMessage.class);
        //Assert

        Assertions.assertEquals(traceId, result.getTraceId());
        Assertions.assertEquals(initiator, result.getInitiator());
        Assertions.assertEquals(messageType, result.getMessageType());
        Assertions.assertEquals(payload, result.getPayload());
    }

    @Test
    void queueMessageListPayloadTest() {
        //Arrange
        String initiator = "aCaller";
        String instanceId = "aInstanceId";
        String messageType = "testType";

        QueueableSearchable queueableSearchable = new QueueableSearchable("index","id",0L, QueueableSearchable.RequestType.INDEX);
        ArrayList<QueueableSearchable> payLoadList = new ArrayList<>();
        payLoadList.add(queueableSearchable);


        QueueMessage expected = new QueueMessage(initiator, instanceId ,messageType, payLoadList);
        //Act

        String jsonString = JsonUtil.toJson(expected);
        QueueMessage result = JsonUtil.fromJson(jsonString, QueueMessage.class);

        //Assert
        Assertions.assertEquals(messageType, result.getMessageType());
        QueueableSearchable resultSearchable = ((ArrayList<QueueableSearchable>) result.getPayload()).get(0);

        Assertions.assertEquals(queueableSearchable.getIndex(), resultSearchable.getIndex());
        Assertions.assertEquals(queueableSearchable.getId(), resultSearchable.getId());
        Assertions.assertEquals(queueableSearchable.getVersion(), resultSearchable.getVersion());
        Assertions.assertEquals(queueableSearchable.getRequestType(), resultSearchable.getRequestType());
    }

}
