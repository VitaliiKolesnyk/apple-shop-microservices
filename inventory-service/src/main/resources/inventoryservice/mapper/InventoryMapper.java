package org.service.inventoryservice.mapper;

import org.service.inventoryservice.dto.InventoryResponse;
import org.service.inventoryservice.entity.Inventory;

import java.util.List;

public interface InventoryMapper {

    InventoryResponse mapToInventoryResponse(Inventory inventory);

    List<InventoryResponse> mapToInventoryResponseList(List<Inventory> productList);
}
