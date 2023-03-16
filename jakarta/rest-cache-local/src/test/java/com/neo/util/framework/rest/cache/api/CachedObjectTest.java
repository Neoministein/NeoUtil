package com.neo.util.framework.rest.cache.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class CachedObjectTest {

     @Test
    void timeSinceCachedTest() throws InterruptedException {
         long cacheCreated = System.currentTimeMillis();

         CachedObject cachedObject = new CachedObject("", cacheCreated);
         Thread.sleep(200);

         Assertions.assertTrue(cachedObject.getTimeSinceCached(TimeUnit.MILLISECONDS) >= 200);
     }
}
