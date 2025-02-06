package org.service.inventoryservice.mapper.impl;

import org.service.inventoryservice.dto.InventoryResponse;
import org.service.inventoryservice.entity.Inventory;
import org.service.inventoryservice.mapper.InventoryMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryMapperImpl implements InventoryMapper {

    @Override
    public InventoryResponse mapToInventoryResponse(Inventory inventory) {
        return new InventoryResponse(inventory.getId(), inventory.getProduct().getSkuCode(), inventory.getProduct().getName(),
                inventory.getProduct().getThumbnailUrl(), inventory.getQuantity(), inventory.getLimit());
    }

    @Override
    public List<InventoryResponse> mapToInventoryResponseList(List<Inventory> productList) {
        return productList.stream()
                .map(this::mapToInventoryResponse)
                .toList();
    }
}
