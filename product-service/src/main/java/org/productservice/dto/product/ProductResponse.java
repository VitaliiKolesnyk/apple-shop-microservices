package org.productservice.dto.product;


import java.io.Serializable;
import java.util.List;

public record ProductResponse(String id, String skuCode, String name,
                              String description, Double price, String thumbnailUrl,
                              List<String> subcategories, boolean isExclusive) implements Serializable {
        }
