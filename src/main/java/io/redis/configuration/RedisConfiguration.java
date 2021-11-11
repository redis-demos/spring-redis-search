package io.redis.configuration;

import io.redis.search.schema.ProductSchema;
import io.redisearch.client.Client;
import io.redisearch.client.IndexDefinition;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisDataException;

import static io.redis.search.schema.ProductSchema.PRODUCT_INDEX;

@EnableRedisRepositories
@Configuration
@Data
@ComponentScan
public class RedisConfiguration {

    @Autowired
    private RedisProperties props;

    // Login to the database and set the password of redis graph with the below command
    // redis-cli$: CONFIG SET requirepass "Redis@password123"

    @Bean(destroyMethod = "close")
    public JedisPool setupJedisPool() {
        return new JedisPool(new JedisPoolConfig(), props.getHost(), props.getPort(), Protocol.DEFAULT_TIMEOUT, props.getPassword());
    }

    @Bean(destroyMethod = "close")
    public Client redisGraphConnection(JedisPool jedisPool, ProductSchema productSchema) {
        Client client = new Client(PRODUCT_INDEX, jedisPool);
        IndexDefinition idxDef = new IndexDefinition().setPrefixes("product");

        try {
            // Create index
            client.createIndex(productSchema.getProductSchema(), Client.IndexOptions.defaultOptions().setDefinition(idxDef));
        } catch (JedisDataException jde) {
            // Ignore error if index already exists
        }
        return client;
    }

    @Bean(destroyMethod = "close")
    public Jedis setupJedis(JedisPool jedisPool) {
        return jedisPool.getResource();
    }

    @Bean
    @Scope("singleton")
    public ProductSchema productSchema() {
        return new ProductSchema();
    }
}
