package com.neo.util.elastic;

import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.framework.api.config.ConfigService;
import com.neo.util.framework.impl.config.BasicConfigService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class ElasticEmbeddedIT {

    ElasticEmbedded subject;

    @Test
    void startupTest() {
        //Arrange
        ConfigService configService = new BasicConfigService();
        configService.save(configService.newConfig("elastic.embedded.dataPath", "./target/esdata"));

        subject = new ElasticEmbedded(configService);

        //Act
        new Thread(() -> subject.startNode()).start();

        //Asset
        IntegrationTestUtil.sleepUntil(1000, 20,() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:9200/_cat/health"))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(200, response.statusCode());
        });



        subject.stopNode();
    }
}
