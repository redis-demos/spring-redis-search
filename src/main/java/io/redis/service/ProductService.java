package io.redis.service;

import io.redis.domain.Product;
import io.redis.repository.ProductRepository;
import io.redis.util.SearchUtil;

import io.redisearch.Query;
import io.redisearch.SearchResult;
import io.redisearch.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class ProductService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    Client redisSearch;

    @Autowired
    SearchUtil utils;

    @GetMapping("/products")
    List<Product> getAllProducts() {
        log.info("Returning all Products...");
        return IterableUtils.toList(productRepository.findAll());
    }

    @GetMapping("/products/{id}")
    Product getProductById(@PathVariable long id) {
        Optional<Product> product = productRepository.findById(String.valueOf(id));

        if (product.isEmpty()) {
            log.info("No product for id {} found", id);
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product entity not found"
            );
        }
        return product.get();
    }

    @PostMapping("/products")
    Product createProduct(@RequestBody Product product) {
        log.info("Creating new product : {}", product.toString());
        return productRepository.save(product);
    }

    @PutMapping("/products")
    Product updateProduct(@RequestBody Product product) {
        log.info("Updating product : {}", product.toString());
        return productRepository.save(product);
    }

    @DeleteMapping("/products/{id}")
    void deleteProductById(@PathVariable long id) {
        log.info("Deleting product with ID : '{}'", id);
        productRepository.deleteById(String.valueOf(id));
    }

    @GetMapping("/products/search")
    List<Product> searchAllProducts(@RequestParam(name = "k") String keyword) {
        log.info("Searching all Products with keyword {}", keyword);

        // execute search
        SearchResult res = redisSearch.search(new Query(keyword));
        log.info("A total of '{}' Search Results have been returned ", res.totalResults);

        // Map to Products POJOs
        List<Product> products = res.docs.stream()
                .map(d -> utils.buildProductFromDoc(d)).collect(Collectors.toList());

        return products;
    }

    @GetMapping("/products/category/{category}")
    List<Product> searchByCategory(@PathVariable String category) {
        log.info("Searching all Products with category {}", category);

        // execute search
        String query = "@category:{" + utils.escapeMetaCharacters(category) + "}";
        log.info("Sanitised Query String: '{}'", query);

        SearchResult res = redisSearch.search(new Query(query));
        log.info("A total of '{}' Search Results have been returned  for the Category search of {}", res.totalResults, query);

        // Map to Products POJOs
        List<Product> products = res.docs.stream()
                .map(d -> utils.buildProductFromDoc(d)).collect(Collectors.toList());

        return products;
    }

    @GetMapping("/products/vendor/{vendor}")
    List<Product> searchByVendor(@PathVariable String vendor) {
        log.info("Searching all Products with vendor {}", vendor);
        return this.searchByField("vendor", vendor);
    }

    @GetMapping("/products/price")
    List<Product> searchByPrice(@RequestParam long min, @RequestParam long max) {
        log.info("Searching all Products with Price range {} to {}", min, max);

        Query query = new Query();
        query.addFilter(new Query.NumericFilter("price", min, max));
        SearchResult res = redisSearch.search(query);
        log.info("A total of '{}' Search Results have been returned for price range search", res.totalResults);

        // Map to Products POJOs
        List<Product> products = res.docs.stream()
                .map(d -> utils.buildProductFromDoc(d)).collect(Collectors.toList());

        return products;
    }


    List<Product> searchByField(String field, String constraint) {
        log.info("Searching all Products on field : '{}' and constraint : {]", field, constraint);

        // execute search
        String query = "@" + field + ":" + utils.escapeMetaCharacters(constraint);
        log.info("Sanitised Query String: '{}'", query);

        SearchResult res = redisSearch.search(new Query(query));
        log.info("A total of '{}' Search Results have been returned for the search criteria of {}", res.totalResults, query);

        // Map to Products POJOs
        List<Product> products = res.docs.stream()
                .map(d -> utils.buildProductFromDoc(d)).collect(Collectors.toList());

        return products;
    }

}
