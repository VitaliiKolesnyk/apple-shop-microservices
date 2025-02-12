package org.service.inventoryservice.service;

import org.service.inventoryservice.dto.InventoryRequest;
import org.service.inventoryservice.dto.InventoryResponse;
import org.service.inventoryservice.entity.Inventory;

import java.util.List;

public interface InventoryService {

    InventoryResponse update(Long id, InventoryRequest inventoryRequest);

    List<InventoryResponse> findAll();

    InventoryResponse findBySkuCode(String skuCode);

    void sendNotification(Inventory inventory);

}
