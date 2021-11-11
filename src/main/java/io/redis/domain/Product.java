package io.redis.domain;

import io.redisearch.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@RedisHash("product")
@TypeAlias("product")
public class Product implements Serializable {

    @Id
    private long id;
    private String name;
    private String description;
    private String vendor;
    private double price;
    private String currency;
    private String category;
    private LinkedList<String> images = new LinkedList<>();

}
