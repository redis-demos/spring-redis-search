package io.redis.service;

import io.redis.RedisContainerInitializer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;


@Slf4j

public class ProductServiceTest extends RedisContainerInitializer {

    @Test
    @DisplayName("Test RedisRepository Get All Products")
    public void testGetAllProducts() {
        String response = given().log().all()
                .when().request("GET", "/products")
                .then().statusCode(200).assertThat()
                .body("size()", is(5))
                .extract().asString();

        assertThat(response, is(notNullValue()));
        // TODO - Do more assertions on returned JSON
    }

    /**
     * Expected Response:
     * {
     *     "id": 999,
     *     "name": "Test",
     *     "description": "This is a description",
     *     "vendor": "Amazon",
     *     "price": 99.0,
     *     "currency": null,
     *     "category": "CATEGORY",
     *     "images": []
     * }
     */
    @Test
    @DisplayName("Test RedisRepository Get Product By ID - 999")
    public void testGetProductById() {
        String response = given().log().all()
                .when().request("GET", "/products/999")
                .then().statusCode(200).log().all().assertThat()
                .body("size()", is(8)) // 8 Fields
                .extract().asString();

        assertThat(response, is(notNullValue()));
        // TODO - Do more assertions on returned JSON
    }
}
