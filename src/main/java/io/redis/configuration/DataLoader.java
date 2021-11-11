package io.redis.configuration;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.redis.domain.Product;
import io.redis.repository.ProductRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private static final String IMAGE_DIR = "src/main/resources/images/products";

    @Value("${spring.redis.seed:false}")
    private boolean load;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    ProductRepository productRepository;

    /**
     * Load JSON file from resources folder and marshall
     * into a List<Product>
     *
     * @return
     * @throws IOException
     */
    private List<Product> readData() throws IOException {
        JavaType type = mapper.getTypeFactory().
                constructCollectionType(List.class, Product.class);
        List<Product> products = mapper.readValue(new File("src/main/resources/data.json"), type);
        Assert.isInstanceOf(List.class, products);

        // Process Images
        products.forEach(p -> this.loadImages(p));

        return products;
    }

    /**
     * Get all images, serialize to byte array and encod to Base64
     * @param product
     */
    private Product loadImages(Product product) {

        try {
            List<File> files = Files.list(Paths.get(IMAGE_DIR + "/" + product.getId()))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            files.forEach(f -> {
                try {
                    // Check for images:  mimetype should be something like "image/png"
                    String mimetype = Files.probeContentType(f.toPath());

                    // Serialize to bytearray
                    if (mimetype != null && mimetype.split("/")[0].equals("image")) {
                        byte[] imageContents = Files.readAllBytes(f.toPath());
                        product.getImages().add(Base64.getEncoder().encodeToString(imageContents));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            // Error while reading the directory or doesnt exist
        }
        return product;
    }

    /**
     * Seed Database with JSON file in resources folder.
     *
     * @param event
     */
    @SneakyThrows
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (load) {
            List<Product> products = this.readData();
            log.info("Seeding Redis with {} Products", products.size());

            products.stream().forEach(p -> {
                log.debug("Saving Product: {}", p);
                productRepository.save(p);
            });

            products = new ArrayList<>();
            productRepository.findAll().forEach(products::add);
            log.info("Saved {} products", products.size());
        }
    }
}
