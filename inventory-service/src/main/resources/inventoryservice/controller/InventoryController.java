package org.service.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.service.inventoryservice.dto.InventoryRequest;
import org.service.inventoryservice.dto.InventoryResponse;
import org.service.inventoryservice.dto.ReserveRequest;
import org.service.inventoryservice.exception.NotInStockException;
import org.service.inventoryservice.service.InventoryService;
import org.service.inventoryservice.service.ProductReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    private final ProductReservationService productReservationService;

    @PostMapping("reserveProducts")
    public ResponseEntity<Boolean> reserveProducts(@RequestBody ReserveRequest reserveRequest) {
        return new ResponseEntity<>(productReservationService.reserveInventory(reserveRequest), HttpStatus.CREATED);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> findAll() {
        return inventoryService.findAll();
    }

    @GetMapping("/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse findBySkuCode(@PathVariable("skuCode") String skuCode) {
        return inventoryService.findBySkuCode(skuCode);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public InventoryResponse update(@PathVariable Long id, @RequestBody InventoryRequest inventoryRequest) {
        return inventoryService.update(id, inventoryRequest);
    }

    @ExceptionHandler(NotInStockException.class)
    public ResponseEntity<Boolean> catchNotInStockException(NotInStockException e) {
        return new ResponseEntity<>(false, HttpStatus.OK);
    }
}
