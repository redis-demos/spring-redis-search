# Getting Started

This project is an example of using Spring Boot + Spring Data along with Redis and Redis Search. 

Some Caveats:
- Redis Search does not OOB Support Spring Data some POJO mapping is done manually

### Reference Documentation
For further reference, please consider the following sections:

* [Redis](https://redis.io/documentation)
* [Redis Search](https://oss.redis.com/redisearch/)
* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.6/maven-plugin/reference/html/)


### Pre-Requirements 
- This project requires a redis instance with the RedisSearch model setup. Using Docker is the easiest approach

```bash
$ docker run -p 6379:6379 redislabs/redisearch:latest
```

### Importing Data into Redis

There are 2 methods to import test data into Redis on this demo. 

1. Use the io.redis.configuration.DataLoader class which runs based off the `spring.redis.seed` flag: 

```bash
#This will load data from src/main/resources/data.json
$ mvn spring-boot:run -D spring.redis.seed=true 

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.5.6)
....
2021-11-11 11:25:14.144  INFO 66081 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2021-11-11 11:25:14.226  INFO 66081 --- [           main] io.redis.configuration.DataLoader        : Seeding Redis with 5 Products
2021-11-11 11:25:14.442  INFO 66081 --- [           main] io.redis.configuration.DataLoader        : Saved 5 products
2021-11-11 11:25:14.444  INFO 66081 --- [           main] io.redis.SpringRedisSearch               : Started SpringRedisSearch in 2.563 seconds (JVM running for 2.865)

```


2. Using Redis In/Out Tools (RIOT)

```bash
$ brew install redis-developer/tap/riot-file
$ riot-file import data/products.csv --header hset --keyspace product --keys product_id 
```

### Running this Example 

- There is a postman collection in `data/spring-redis-search.postman_collection.json`

There are Currently 9 API Endpoints: 

1. Get All Products:
```bash
$ curl --location --request GET 'http://localhost:8080/products'
```
2. Get Product By ID
```bash
$ curl --location --request GET 'http://localhost:8080/products/{id}'
```
3. Save Product
```bash
$ curl --location --request POST 'http://localhost:8080/products' \
--header 'Content-Type: application/json' \
--data-raw '  {
    "id": "111",
    "price": "1000.00",
    "vendor": "Apple",
    "name": "Apple Ipad",
    "description": "2022 Apple iPad (10.2-inch iPad, Wi-Fi, 64GB) - Space Grey (10th Generation)",
    "currency": "GBP",
    "category": "Tablet"
  }'
```
4. Update Product
```bash
$ curl --location --request PUT 'http://localhost:8080/products' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": 111,
    "name": "Apple Ipad",
    "description": "2022 Apple iPad (10.2-inch iPad, Wi-Fi, 64GB) - Space Grey (10th Generation)",
    "vendor": "Apple",
    "price": 1099.0,
    "currency": "GBP",
    "category": "Tablet"
}'
```
5. Delete Product By ID
```bash
$ curl --location --request DELETE 'http://localhost:8080/products/111'
```
6. Full Text Search for Product i.e. across al fields
```bash
$ curl --location --request GET 'http://localhost:8080/products/search?k={SEARCH_TERM}'
```
7. Search By Product Category
```bash
$ curl --location --request GET 'http://localhost:8080/products/category/{CATEGORY}'
```
8. Search By Product Vendor
```bash
$ curl --location --request GET 'http://localhost:8080/products/vendor/amazon'
```
9. Search By Price Range 
```bash
$ curl --location --request GET 'http://localhost:8080/products/price?min={MIN_PRICE}&max=MAX_PRICE'
```