package io.redis.search.schema;

import io.redisearch.Schema;
import lombok.Getter;

@Getter
public class ProductSchema {

    public final static String PRODUCT_INDEX = "idx:product";

    private Schema productSchema;

    // $ FT.CREATE idx:product ON hash PREFIX 1 "product:"
    // SCHEMA name TEXT SORTABLE
    // description TEXT
    // vendor TEXT SORTABLE
    // price NUMERIC SORTABLE
    // category TAG SORTABLE
    public ProductSchema(){
        productSchema = new Schema();
        productSchema.addSortableTextField("name",  1.0);
        productSchema.addSortableTextField("vendor", 1.0);
        productSchema.addTextField("description", 2.0);
        productSchema.addSortableNumericField("price");
        productSchema.addSortableTagField("category", ",");
    }
}
