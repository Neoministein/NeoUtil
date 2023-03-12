package com.neo.util.framework.rest.impl.cache;

import com.neo.util.framework.rest.cache.impl.CaffeineServerCache;
import com.neo.util.framework.rest.impl.AbstractIntegrationTest;
import io.helidon.microprofile.server.JaxRsCdiExtension;
import io.helidon.microprofile.server.ServerCdiExtension;
import io.helidon.microprofile.tests.junit5.AddBean;
import io.helidon.microprofile.tests.junit5.AddExtension;
import io.helidon.microprofile.tests.junit5.DisableDiscovery;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.ext.cdi1x.internal.CdiComponentProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@HelidonTest
@AddExtension(ServerCdiExtension.class)
@AddExtension(JaxRsCdiExtension.class)
@AddExtension(CdiComponentProvider.class)
@AddBean(CachedResource.class)
@AddBean(CaffeineServerCache.class)
@AddBean(ClientCacheControlFilter.class)
@DisableDiscovery
class CaffeineServerCacheIT extends AbstractIntegrationTest {

    @Inject
    protected WebTarget webTarget;

    @Test
    void test() {
        //Arrange

        //Act
        Response response = webTarget.path(CachedResource.RESOURCE_LOCATION).request().method("GET");
        //Assert

        Assertions.assertEquals(200, response.getStatus());
        String body = response.readEntity(String.class);
        Assertions.assertEquals("Test", body);
    }

}
