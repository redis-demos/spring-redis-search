package io.redis;

import junit.framework.Assert;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
public class RedisContainerTest extends RedisContainerInitializer {

    @Test
    @DisplayName("Test RedisGraph Container is Running")
    public void containerStartsAndPublicPortIsAvailable() throws Exception {
        assertThat(true, is(redis.isRunning()));
        assertThat("localhost", is(redis.getHost()));
    }
}
