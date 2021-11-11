## Data Modelling Homework

### Question: 

#### Logical Data Model 

The logical data model is separate from the DBMS being used...it defines the structure of data elements and to set relationships between them.

- 1 `Product` to many `Product Images`
- 1 `Product` (MainCategory) to 1 `Category`
- 1 `Category` to many `Product's`

![Class Diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/redis-demos/spring-redis-search/master/data/erd.puml)

Or the raw data structure: 

    Product Image
        Id : Number
        Value : Binary

    Product
        Id : Number
        Name : String
        Description: String
        Vendor : String
        Price : Number
        Currency : String
        MainCategory : Category (1)
        Images : Image (0..n)

    Category
        Id : Number
        Name : String
        Products : Product (0..n)


#### Redis Data Requirements Requirements

   - Product information stored in the database should include: name, description, vendor, price, category, images associated with that product
   - Ability to create/update/delete product details
   - Ability to find product by ID 
   - Ability to find products in category X 
   - Ability to find product by it's name or part of it's name

### Design & Implementation: 

#### Design 
- Store data within a single Redis `Hash` including images 
  - Considered storing images in a separate List/Set
- Create Secondary Indexes for enhanced Search Capabilities

- Roll the ERD diagram into a flat hash structure

![Class Diagram](http://www.plantuml.com/plantuml/proxy?src=https://raw.githubusercontent.com/redis-demos/spring-redis-search/master/data/hash.puml)

#### Implementation 

- Install / Configure a RedisSearch feature locally

```bash
$ docker run -p 6379:6379 redislabs/redisearch:latest
$ ./redis-cli 
```

- Create/Insert data into Redis:
  - `HSET <key> name <value> description <value> vendor <value> price <value> currency <value> category <value> image[...] <value>`

```bash
$ HSET product:001 name "Kindle Paperwhite" description "Waterproof, 6 inch High-Resolution Display, 32 GB, Black" vendor "Amazon" price 79.99 currency "GBP" category "E-Reader"
$ HSET product:011 name "Kindle Fire 7 Tablet" description "7 inch display, 16 GB, Black" vendor "Amazon" price 49.99 currency "GBP" category "Tablet"
$ HSET product:012 name "Apple Ipad" description "2021 Apple iPad (10.2-inch iPad, Wi-Fi, 64GB) - Space Grey (9th Generation)" vendor "Apple" price 319.00 currency "GBP" category "Tablet"
$ HSET product:013 name "Apple Ipad Air" description "2020 Apple iPad Air (10.9-inch, Wi-Fi, 64GB) - Space Grey (4th Generation)" vendor "Apple" price 549.98 currency "GBP" category "Tablet"
$ HSET product:014 name "Samsung Galaxy Tab A7" description "Samsung Galaxy Tab A7 32 GB Wi-Fi Android Tablet - Dark Grey (UK Version)" vendor "Samsung" price 194.00 currency "GBP" category "Tablet"
```

- Create Index with the following fields:
  - `name` : (string) Product Name 
  - `description` : (string) A summary of the product. 
  - `vendor` : (string) Product vendor 
  - `price` : (number) Numerical value/cost/price 
  - `category` : (string) product category
  
```bash
$ FT.CREATE idx:product ON hash PREFIX 1 "product:" SCHEMA name TEXT SORTABLE description TEXT vendor TEXT SORTABLE price NUMERIC SORTABLE category TAG SORTABLE
```

---
**NOTE**

`category` is defined using [TAG](https://oss.redis.com/redisearch/Tags/) for tokenization searches

---

## Results  

- Product information stored in the database should include: name, description, vendor, price, category, images associated with that product ✅ (fulfilled by design)


### Ability to create/update/delete product details

```bash
// Get 
$ HMGET product:011 name
1) "Kindle Fire 7 Tablet"
$ HMGET product:012 name
1) "Apple Ipad"
$ HMGET product:012 price
1) "319.00"

// Update
$ HSET product:001 image "XXXXX"
$ HMGET product:001 image
1) "XXXXX"

// Delete
$ HSET product:015 name "Testing" description "Samsung Galaxy Tab A7 32 GB Wi-Fi Android Tablet - Dark Grey (UK Version)" vendor "Samsung" price 194.00 currency "GBP" category "Tablet"
(integer) 6
$ HMGET product:015 name
1) "Testing"
$ HDEL product:015 name
(integer) 1
$ HMGET product:015 name
1) (nil)

// Delete Record
$ DEL product:015
(integer) 1
$ HGETALL product:015
(empty array)
```
#### REST 

1. Get Product By ID
```bash
$ curl --location --request GET 'http://localhost:8080/products/{id}'
```
2. Save Product
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
3. Update Product
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
4. Delete Product By ID
```bash
$ curl --location --request DELETE 'http://localhost:8080/products/111'

### Ability to find product by ID

```bash
// Find by Product ID
$ HGET/HMGET/HGETALL product:<PRODUCT_ID> <field..n>
$ HMGET product:001 name category
1) "Kindle Paperwhite"
2) "E-Reader"
```

### Ability to find products in category X

```bash
// Find by Product ID
$ FT.SEARCH idx:product "@category:{e\\-reader}" 
$ FT.SEARCH idx:product "@category:{Tablet}" 
1) (integer) 1
2) "product:001"
3)  1) "name"
    2) "Kindle Paperwhite"
    3) "description"
    4) "Waterproof, 6 inch High-Resolution Display, 32 GB, Black"
    5) "vendor"
    6) "Amazon"
    7) "price"
    8) "79.99"
    9) "currency"
   10) "GBP"
   11) "category"
   12) "E-Reader"
   13) "image"
   14) "XXXXX"
```

#### REST

1. Search By Product Category
```bash
$ curl --location --request GET 'http://localhost:8080/products/category/{CATEGORY}'
```

### Ability to find product by it's name or part of it's name

```bash
$ FT.SEARCH idx:product "@name:Ipad" 
1) (integer) 2
2) "product:012"
3)  1) "currency"
    2) "GBP"
    3) "category"
    4) "Tablet"
    5) "description"
    6) "2021 Apple iPad (10.2-inch iPad, Wi-Fi, 64GB) - Space Grey (9th Generation)"
    7) "vendor"
    8) "Apple"
    9) "price"
   10) "319.00"
   11) "name"
   12) "Apple Ipad"
4) "product:013"
5)  1) "currency"
    2) "GBP"
    3) "category"
    4) "Tablet"
    5) "description"
    6) "2020 Apple iPad Air (10.9-inch, Wi-Fi, 64GB) - Space Grey (4th Generation)"
    7) "vendor"
    8) "Apple"
    9) "price"
   10) "549.98"
   11) "name"
   12) "Apple Ipad Air"
```
#### REST
2. Full Text Search for Product i.e. across al fields
```bash
$ curl --location --request GET 'http://localhost:8080/products/search?k={SEARCH_TERM}'
```

### Other Implemented REST Endpoints

1. Get All Products:
```bash
$ curl --location --request GET 'http://localhost:8080/products'
```
2. Search By Product Vendor
```bash
$ curl --location --request GET 'http://localhost:8080/products/vendor/amazon'
```
3. Search By Price Range
```bash
$ curl --location --request GET 'http://localhost:8080/products/price?min={MIN_PRICE}&max=MAX_PRICE'
```
