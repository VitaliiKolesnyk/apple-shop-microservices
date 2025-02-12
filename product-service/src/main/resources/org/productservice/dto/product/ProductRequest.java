package org.productservice.dto.product;


public record ProductRequest(String skuCode, String name, String description,
                             Double price, String categories, boolean isExclusive) {
        }
