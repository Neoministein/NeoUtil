package com.neo.util.jakarta.elastic;

import com.neo.util.api.config.ConfigService;
import com.neo.util.api.config.DefaultConfigService;
import com.neo.util.common.impl.test.IntegrationTestUtil;
import com.neo.util.jakarta.config.InMemoryConfigStore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

class ElasticEmbeddedIT {

    EmbeddedElasticNode subject;

    @Test
    void startupTest() {
        //Arrange
        ConfigService configService = new DefaultConfigService(List.of(new InMemoryConfigStore()));
        configService.save("./target/esdata", "elastic.embedded.dataPath");

        subject = new EmbeddedElasticNode(configService);

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
