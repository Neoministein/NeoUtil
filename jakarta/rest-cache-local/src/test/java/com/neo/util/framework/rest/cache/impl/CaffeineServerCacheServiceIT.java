package com.neo.util.framework.rest.cache.impl;

import com.neo.util.framework.rest.cache.api.CachedObject;
import com.neo.util.framework.rest.cache.api.ServerCacheService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class CaffeineServerCacheServiceIT {

    ServerCacheService subject;

    TestConfigService testConfigService = new TestConfigService();

    @BeforeEach
    void before() {
        CaffeineServerCacheService subject = Mockito.spy(CaffeineServerCacheService.class);
        subject.configService = testConfigService;

        subject.init();
        this.subject = subject;
    }

    @Test
    void cachingTest() {
        //Arrange
        CachedObject originalObjectA = new CachedObject(10);
        CachedObject originalObjectB = new CachedObject(5);

        //Act
        subject.put("aKey", originalObjectA);
        subject.put("bKey", originalObjectB);

        Optional<CachedObject> cachedObjectA = subject.get("aKey");
        Optional<CachedObject> cachedObjectB = subject.get("bKey");

        //Assert
        Assertions.assertEquals(originalObjectA.entity(), cachedObjectA.get().entity());
        Assertions.assertEquals(originalObjectB.entity(), cachedObjectB.get().entity());
    }

    @Test
    void invalidationTest() {
        //Arrange
        CachedObject originalObjectA = new CachedObject(10);
        CachedObject originalObjectB = new CachedObject(5);

        //Act
        subject.put("aKey", originalObjectA);
        subject.put("bKey", originalObjectB);

        Optional<CachedObject> cachedObjectA = subject.get("aKey");
        subject.invalidate("aKey");
        Optional<CachedObject> invalidatedCacheObjectA = subject.get("aKey");

        Optional<CachedObject> cachedObjectB = subject.get("bKey");
        //Assert
        Assertions.assertEquals(originalObjectA.entity(), cachedObjectA.get().entity());
        Assertions.assertTrue(invalidatedCacheObjectA.isEmpty());
        Assertions.assertEquals(originalObjectB.entity(), cachedObjectB.get().entity());
    }

    @Test
    void invalidationAllTest() {
        //Arrange
        CachedObject originalObjectA = new CachedObject(10);
        CachedObject originalObjectB = new CachedObject(5);

        //Act
        subject.put("aKey", originalObjectA);
        subject.put("bKey", originalObjectB);

        subject.invalidateAll();
        Optional<CachedObject> invalidatedCacheObjectA = subject.get("aKey");

        Optional<CachedObject> invalidatedCacheObjectB = subject.get("bKey");
        //Assert
        Assertions.assertTrue(invalidatedCacheObjectA.isEmpty());
        Assertions.assertTrue(invalidatedCacheObjectB.isEmpty());
    }

    @Test
    void putAllTest() {
        //Arrange
        CachedObject originalObjectA = new CachedObject(10);
        CachedObject originalObjectB = new CachedObject(5);

        //Act
        subject.putAll(Map.of("aKey", originalObjectA, "bKey", originalObjectB));

        Optional<CachedObject> cachedObjectA = subject.get("aKey");
        Optional<CachedObject> cachedObjectB = subject.get("bKey");

        //Assert
        Assertions.assertEquals(originalObjectA.entity(), cachedObjectA.get().entity());
        Assertions.assertEquals(originalObjectB.entity(), cachedObjectB.get().entity());
    }

    @Test
    void invalidateIteratorTest() {
        //Arrange
        CachedObject originalObjectA = new CachedObject(10);
        CachedObject originalObjectB = new CachedObject(5);

        //Act
        subject.put("aKey", originalObjectA);
        subject.put("bKey", originalObjectB);

        subject.invalidateAll(Set.of("aKey", "bKey"));
        Optional<CachedObject> invalidatedCacheObjectA = subject.get("aKey");

        Optional<CachedObject> invalidatedCacheObjectB = subject.get("bKey");
        //Assert
        Assertions.assertTrue(invalidatedCacheObjectA.isEmpty());
        Assertions.assertTrue(invalidatedCacheObjectB.isEmpty());
    }

    @Test
    void maxAgeTest() {
        //Arrange
        long twentySeconds = Duration.ofSeconds(20).toMillis();
        long fiveSeconds = Duration.ofSeconds(5).toMillis();

        CachedObject originalObjectA = new CachedObject(10, System.currentTimeMillis() - twentySeconds);
        CachedObject originalObjectB = new CachedObject(5, System.currentTimeMillis() - fiveSeconds);

        //Act
        subject.put("aKey", originalObjectA);
        subject.put("bKey", originalObjectB);

        Optional<CachedObject> invalidatedCacheObjectA = subject.getMaxAge("aKey", TimeUnit.SECONDS, 10);

        Optional<CachedObject> invalidatedCacheObjectB = subject.getMaxAge("bKey", Duration.ofSeconds(10));
        //Assert
        Assertions.assertTrue(invalidatedCacheObjectA.isEmpty());
        Assertions.assertTrue(invalidatedCacheObjectB.isPresent());
    }
}
