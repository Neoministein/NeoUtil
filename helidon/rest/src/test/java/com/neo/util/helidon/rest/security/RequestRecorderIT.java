package com.neo.util.helidon.rest.security;

import com.neo.util.framework.impl.persistence.search.DummySearchProvider;
import com.neo.util.framework.rest.impl.security.RequestRecorder;
import com.neo.util.framework.rest.percistence.RequestSearchable;
import com.neo.util.helidon.rest.AbstractIntegrationTest;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@HelidonTest
class RequestRecorderIT extends AbstractIntegrationTest {


    @Inject
    DummySearchProvider searchProvider;

    @BeforeEach
    void before() {
        searchProvider.setEnabled(true);
    }

    @Test
    void basicNonSecuredRecordingTest(WebTarget webTarget) {
        //Arrange

        //Act
        webTarget.path(SecurityResource.RESOURCE_LOCATION).request().method("GET");
        //Assert
        RequestSearchable searchable = (RequestSearchable) searchProvider.getSearchableToIndex();
        Assertions.assertNotNull(searchable);

        Assertions.assertNotNull(searchable.getRequestId());
        Assertions.assertNull(searchable.getOwner());
        Assertions.assertEquals("127.0.0.1", searchable.getRemoteAddress());
        Assertions.assertEquals("GET test/security", searchable.getContext());
        Assertions.assertEquals("200", searchable.getStatus());
        Assertions.assertNull(searchable.getError());
        Assertions.assertTrue(searchable.getProcessTime() >= 0);
        Assertions.assertNotNull(searchable.getAgent());
        Assertions.assertNotNull(searchable.getCreationDate());
    }

    @Test
    void securedRecordingTest(WebTarget webTarget) {
        //Arrange

        //Act
        webTarget.path(SecurityResource.RESOURCE_LOCATION + SecurityResource.P_SECURED).request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + BasicAuthorizationProvider.NORMAL_TOKEN)
                .method("GET");

        //Assert
        RequestSearchable searchable = (RequestSearchable) searchProvider.getSearchableToIndex();
        Assertions.assertNotNull(searchable);

        Assertions.assertNotNull(searchable.getRequestId());
        Assertions.assertEquals(BasicAuthorizationProvider.NORMAL_PRINCIPAL.getName(), searchable.getOwner());
        Assertions.assertEquals("127.0.0.1", searchable.getRemoteAddress());
        Assertions.assertEquals("GET test/security/secure", searchable.getContext());
        Assertions.assertEquals("200", searchable.getStatus());
        Assertions.assertNull(searchable.getError());
        Assertions.assertTrue(searchable.getProcessTime() >= 0);
        Assertions.assertNotNull(searchable.getAgent());
        Assertions.assertNotNull(searchable.getCreationDate());
    }

    @Test
    void invalidMethodRecordingTest(WebTarget webTarget) {
        //Arrange

        //Act
        webTarget.path(SecurityResource.RESOURCE_LOCATION + SecurityResource.P_SECURED).request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + BasicAuthorizationProvider.NORMAL_TOKEN)
                .method("POST");

        //Assert
        RequestSearchable searchable = (RequestSearchable) searchProvider.getSearchableToIndex();
        Assertions.assertNotNull(searchable);

        Assertions.assertNotNull(searchable.getRequestId());
        Assertions.assertEquals(BasicAuthorizationProvider.NORMAL_PRINCIPAL.getName(), searchable.getOwner());
        Assertions.assertEquals("127.0.0.1", searchable.getRemoteAddress());
        Assertions.assertEquals("POST test/security/secure", searchable.getContext());
        Assertions.assertEquals("405", searchable.getStatus());
        Assertions.assertEquals(RequestRecorder.FRAMEWORK_PROVIDED_ERROR, searchable.getError());
        Assertions.assertTrue(searchable.getProcessTime() >= 0);
        Assertions.assertNotNull(searchable.getAgent());
        Assertions.assertNotNull(searchable.getCreationDate());
    }

    @Test
    void invalidResourceRecordingTest(WebTarget webTarget) {
        //Arrange

        //Act
        webTarget.path(SecurityResource.RESOURCE_LOCATION).request().method("POST");

        webTarget.path(SecurityResource.RESOURCE_LOCATION + "/unknown").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + BasicAuthorizationProvider.NORMAL_TOKEN)
                .method("GET");

        //Assert
        RequestSearchable searchable = (RequestSearchable) searchProvider.getSearchableToIndex();
        Assertions.assertNotNull(searchable);

        Assertions.assertNotNull(searchable.getRequestId());
        Assertions.assertEquals(BasicAuthorizationProvider.NORMAL_PRINCIPAL.getName(), searchable.getOwner());
        Assertions.assertEquals("127.0.0.1", searchable.getRemoteAddress());
        Assertions.assertEquals("GET test/security/unknown", searchable.getContext());
        Assertions.assertEquals("404", searchable.getStatus());
        Assertions.assertEquals(RequestRecorder.FRAMEWORK_PROVIDED_ERROR, searchable.getError());
        Assertions.assertTrue(searchable.getProcessTime() >= 0);
        Assertions.assertNotNull(searchable.getAgent());
        Assertions.assertNotNull(searchable.getCreationDate());
    }

    @Test
    void invalidResourceNoUserRecordingTest(WebTarget webTarget) {
        //Arrange

        //Act
        webTarget.path(SecurityResource.RESOURCE_LOCATION).request().method("POST");

        webTarget.path(SecurityResource.RESOURCE_LOCATION + "/unknown").request()
                .method("GET");

        //Assert
        RequestSearchable searchable = (RequestSearchable) searchProvider.getSearchableToIndex();
        Assertions.assertNotNull(searchable);

        Assertions.assertNotNull(searchable.getRequestId());
        Assertions.assertNull(searchable.getOwner());
        Assertions.assertEquals("127.0.0.1", searchable.getRemoteAddress());
        Assertions.assertEquals("GET test/security/unknown", searchable.getContext());
        Assertions.assertEquals("404", searchable.getStatus());
        Assertions.assertEquals(RequestRecorder.FRAMEWORK_PROVIDED_ERROR, searchable.getError());
        Assertions.assertTrue(searchable.getProcessTime() >= 0);
        Assertions.assertNotNull(searchable.getAgent());
        Assertions.assertNotNull(searchable.getCreationDate());
    }
}
