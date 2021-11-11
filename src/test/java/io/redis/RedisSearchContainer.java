package io.redis;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class RedisSearchContainer extends GenericContainer<RedisSearchContainer> {

    public static final int REDIS_PORT = 6379;
    public static final String DEFAULT_IMAGE_AND_TAG = "redislabs/redisearch:latest";

    public RedisSearchContainer() {
        this(DEFAULT_IMAGE_AND_TAG);
    }

    public RedisSearchContainer(@NotNull String image) {
        super(image);
        addExposedPort(REDIS_PORT);
        withExposedPorts(REDIS_PORT);
        withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(
                RedisSearchContainer.class)));
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }

    @NotNull
    public Integer getPort() {
        return getMappedPort(REDIS_PORT);
    }
}
