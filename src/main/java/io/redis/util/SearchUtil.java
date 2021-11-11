package io.redis.util;

import io.redis.domain.Product;
import io.redisearch.Document;
import org.springframework.stereotype.Component;

@Component
public class SearchUtil {

    /**
     * Basic POJO builder from redis io.redisearch.Document
     * @param doc
     * @return
     */
    public Product buildProductFromDoc(Document doc) {
        Product p = new Product();
        p.setId(Long.valueOf((String) doc.get("id")));
        p.setName((String) doc.get("name"));
        p.setDescription((String) doc.get("description"));
        p.setVendor((String) doc.get("vendor"));
        p.setPrice(Double.valueOf((String) doc.get("price")));
        p.setCurrency((String) doc.get("currency"));
        p.setCategory((String) doc.get("category"));

        // TODO - Cast Images to Linked List

        return p;
    }

    /**
     * Add escape symbols for 'Special Characters' in Strings
     * @param inputString
     * @return
     */
    public String escapeMetaCharacters(String inputString){
        final String[] metaCharacters = {"\\","^","$","{","}","[","]","(",")",".","*","+","?","|","<",">","-","&","%"};

        for (int i = 0 ; i < metaCharacters.length ; i++){
            if(inputString.contains(metaCharacters[i])){
                inputString = inputString.replace(metaCharacters[i],"\\"+metaCharacters[i]);
            }
        }
        return inputString;
    }

}
