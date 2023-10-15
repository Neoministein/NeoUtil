package com.neo.util.helidon.rest.cache;

import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.testing.junit5.AddBean;
import io.helidon.microprofile.testing.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@HelidonTest
@AddBean(CacheControlFilterResource.class)
class CacheControlFilterIT extends AbstractIntegrationTest {

    @Inject
    protected WebTarget webTarget;


    @Test
    void maxAgeTest() {
        //Arrange

        //Act
        Response response = webTarget.path(CacheControlFilterResource.RESOURCE_LOCATION + CacheControlFilterResource.P_MAX_AGE).request().method("GET");
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        String body = response.readEntity(String.class);
        Assertions.assertEquals(CacheControlFilterResource.ENTITY, body);
        Assertions.assertEquals("max-age=86400",response.getHeaderString(HttpHeaders.CACHE_CONTROL));
    }
}
