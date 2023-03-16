package com.neo.util.framework.rest.cache.impl.filter;

import com.neo.util.framework.rest.cache.impl.TestConfigService;
import com.neo.util.framework.rest.cache.impl.ServerCacheFilter;
import com.neo.util.framework.rest.cache.impl.AbstractIntegrationTest;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@HelidonTest(resetPerTest = true)
@AddBean(TestConfigService.class)
@AddBean(CachedResource.class)
class ServerCacheFilterIT extends AbstractIntegrationTest {

    @Test
    void cachedValueTest(WebTarget webTarget) {
        //Arrange
        WebTarget endpoint = webTarget.path(CachedResource.RESOURCE_LOCATION + CachedResource.P_INCREMENTAL_VALUE);

        //Act
        Response notCached = endpoint.request().method("GET");
        Response cachedValue = endpoint.request().method("GET");

        //Assert
        Assertions.assertEquals(200, notCached.getStatus());
        Assertions.assertEquals(0, notCached.readEntity(Integer.class));

        Assertions.assertEquals(200, cachedValue.getStatus());
        Assertions.assertEquals(0, cachedValue.readEntity(Integer.class));
    }

    @Test
    void skipCacheTest(WebTarget webTarget) {
        //Arrange
        WebTarget endpoint = webTarget.path(CachedResource.RESOURCE_LOCATION + CachedResource.P_INCREMENTAL_VALUE);

        //Act
        Response notCached = endpoint.request().header(ServerCacheFilter.HEADER_NO_CACHE, "").method("GET");
        Response cachedValue = endpoint.request().header(ServerCacheFilter.HEADER_NO_CACHE, "").method("GET");

        //Assert
        Assertions.assertEquals(200, notCached.getStatus());
        Assertions.assertEquals(0, notCached.readEntity(Integer.class));

        Assertions.assertEquals(200, cachedValue.getStatus());
        Assertions.assertEquals(1, cachedValue.readEntity(Integer.class));
    }

    @Test
    void cacheExpiredTestTest(WebTarget webTarget) {
        //Arrange
        WebTarget endpoint = webTarget.path(CachedResource.RESOURCE_LOCATION + CachedResource.P_INCREMENTAL_VALUE);

        //Act
        Response notCached = endpoint.request().header(ServerCacheFilter.HEADER_NO_CACHE, "").method("GET");
        Response cachedValue = endpoint.request().header(ServerCacheFilter.HEADER_NO_CACHE, "").method("GET");

        //Assert
        Assertions.assertEquals(200, notCached.getStatus());
        Assertions.assertEquals(0, notCached.readEntity(Integer.class));

        Assertions.assertEquals(200, cachedValue.getStatus());
        Assertions.assertEquals(1, cachedValue.readEntity(Integer.class));
    }
}
