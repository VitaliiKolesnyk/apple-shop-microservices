import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule  } from "@angular/forms";
import { InventoryService } from '../services/inventory.service';
import { NgIf } from "@angular/common";
import { FormsModule } from '@angular/forms'; // Import FormsModule

@Component({
  selector: 'app-add-inventory',
  standalone: true,
  imports: [NgIf, FormsModule, ReactiveFormsModule],
  templateUrl: './add-inventory.component.html',
  styleUrls: ['./add-inventory.component.css']
})
export class AddInventoryComponent {
  addInventoryForm: FormGroup;
  private readonly inventoryService = inject(InventoryService);
  inventoryCreated = false;
  errorMessage: string = '';

  constructor(private fb: FormBuilder) {
    this.addInventoryForm = this.fb.group({
      skuCode: ['', [Validators.required]],
      quantity: [0, [Validators.required, Validators.min(1)]]
    });
  }

  onSubmit(): void {
    if (this.addInventoryForm.valid) {
      const inventory = {
        skuCode: this.addInventoryForm.get('skuCode')?.value,
        quantity: this.addInventoryForm.get('quantity')?.value,
      };

      this.inventoryService.createInventory(inventory).subscribe(
        () => {
          this.inventoryCreated = true;
          this.addInventoryForm.reset(); // Reset the form
        },
        (error) => {
          this.errorMessage = error; // Set the error message to be displayed
          console.error('Inventory creation failed', error);
        }
      );
    } else {
      console.log('Form is not valid');
    }
  }

  get skuCode() {
    return this.addInventoryForm.get('skuCode');
  }

  get quantity() {
    return this.addInventoryForm.get('quantity');
  }
}
