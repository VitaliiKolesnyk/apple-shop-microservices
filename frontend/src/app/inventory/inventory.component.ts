import { Component, OnInit } from '@angular/core';
import { InventoryService } from '../services/inventory.service';
import { InventoryDto } from '../inventory-dto';
import { FormsModule } from '@angular/forms'; // Import FormsModule
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
  styleUrls: ['./inventory.component.css'],
  standalone: true,
  imports: [FormsModule, CommonModule]
})
export class InventoryComponent implements OnInit {
  inventories: InventoryDto[] = [];
  successMessage: string = '';
  errorMessage: string = '';
  skuCode: string = '';

  constructor(private inventoryService: InventoryService) {}

  ngOnInit(): void {
    this.loadInventories();
  }

  loadInventories(): void {
    this.inventoryService.getAllInventories().subscribe((data) => {
      this.inventories = data;
      this.errorMessage = '';
    });
  }

  searchBySkuCode(): void {
    if (this.skuCode.trim()) {
      this.inventoryService.getBySkuCode(this.skuCode).subscribe(
        (inventory) => {
          this.inventories = [inventory];  // Display the found inventory
          this.errorMessage = '';  // Clear error message
        },
        (error) => {
          this.inventories = [];  // Clear inventories if no result
          this.errorMessage = `No inventory found with SKU code ${this.skuCode}`;  // Set error message
        }
      );
    } else {
      this.loadInventories();  // Reset to show all inventories if search field is empty
    }
  }

  updateInventory(inventory: InventoryDto): void {
    // Send the updated inventory
    const updatedInventory: InventoryDto = {
      id: inventory.id,
      skuCode: inventory.skuCode,
      name: inventory.name,
      thumbnailUrl: inventory.thumbnailUrl,
      quantity: inventory.quantity,    // Use the quantity input directly
      limit: inventory.limit           // Use the limit input directly
    };

    this.inventoryService.updateInventory(updatedInventory).subscribe(() => {
      this.loadInventories(); // Reload inventories after update
      this.successMessage = 'Inventory updated successfully!';

      // Clear the success message after 3 seconds
      setTimeout(() => {
        this.successMessage = '';
      }, 3000);
    });
  }
}
