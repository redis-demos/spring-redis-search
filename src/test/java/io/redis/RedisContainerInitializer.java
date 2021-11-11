package io.redis;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
@ContextConfiguration(initializers = RedisContainerInitializer.RedisInitializer.class)
public class RedisContainerInitializer {

    @Autowired
    Environment environment;

    public static RedisSearchContainer redis;

    /**
     * Setup Redis TestContainer/Docker instance
     */
    @BeforeAll
    public static void startContainerAndPublicPortIsAvailable() {
        redis = new RedisSearchContainer();
        redis.start();
        log.info("Redis Container running @ Port={}, Host={}", redis.getPort(), redis.getHost());
    }

    /**
     * This sucks but it's not working other methods for some reason...
     */
    @BeforeEach
    public void setUp(){
        RestAssured.port = Integer.valueOf(environment.getProperty("local.server.port"));
        RestAssured.baseURI = "http://localhost";
    }

    /**
     * Override Spring Props from TestContainer Instance
     */
    public static class RedisInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            log.info("Overriding Spring Properties for redis");

            TestPropertyValues values = TestPropertyValues.of(
                    "spring.redis.host=" + redis.getContainerIpAddress(),
                    "spring.redis.port=" + redis.getPort()
            );
            values.applyTo(configurableApplicationContext);
        }
    }
}
