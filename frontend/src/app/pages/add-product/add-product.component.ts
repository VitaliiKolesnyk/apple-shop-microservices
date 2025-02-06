import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProductService } from '../../services/product.service';
import { CategoryService } from '../../services/category.service';
import { Category, Subcategory } from '../../category';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-add-product',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './add-product.component.html',
  styleUrls: ['./add-product.component.css']
})
export class AddProductComponent implements OnInit {
  addProductForm: FormGroup;
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  errorMessage: string = '';
  successMessage: string = '';
  selectedFile: File | null = null;
  animatedMessage: { type: string; text: string } | null = null;
  imagePreview: string | ArrayBuffer | null = null;
  categories: Category[] = [];
  subcategoryLevels: any[] = [];
  globalCategoryId: string | null = null;

  constructor(private fb: FormBuilder) {
    this.addProductForm = this.fb.group({
      skuCode: ['', [Validators.required, Validators.minLength(3)]],
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      price: [0, [Validators.required, Validators.min(0)]],
      parentCategoryId: ['', Validators.required],
      subcategoryId: ['', Validators.required],
      isExclusive: [false]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    // Load categories when component initializes
    this.categoryService.getCategories().subscribe({
      next: (categories) => {
        // Filter categories to show only main categories (isMain = true)
        this.categories = categories.filter(category => category.isMain);
        // Initialize subcategoryId control for the first time
        if (this.categories.length) {
          this.addProductForm.get('subcategoryId')?.setValue(null); // Ensure it's reset when categories are loaded
        }
      },
      error: (error: HttpErrorResponse) => {
        console.error('Failed to load categories:', error);
      }
    });
  }

  onParentCategoryChange(): void {
    const selectedCategoryId = this.addProductForm.get('parentCategoryId')?.value;
    const selectedCategory = this.categories.find(category => category.id === selectedCategoryId);

    if (selectedCategory) {
      this.globalCategoryId = selectedCategory.id;
      // Reset only the subcategory fields, not other fields like price, description
      this.addProductForm.get('subcategoryId')?.setValue(null);
      this.resetSubcategoryControls();  // Reset dynamically created subcategory controls

      // Clear the subcategory levels for the selected category
      this.subcategoryLevels = [];
      this.loadSubcategories(selectedCategory.subcategories, 0); // Populate subcategories based on selected parent category
    }
  }

  resetSubcategoryControls(): void {
    // Reset dynamically added subcategory controls from the form
    for (let i = 0; i < this.subcategoryLevels.length; i++) {
      this.addProductForm.get(`subcategoryId${i}`)?.setValue(null);
    }
  }

  loadSubcategories(subcategories: Category[] | Subcategory[], levelIndex: number): void {
    if (subcategories && subcategories.length) {
      // Assign subcategories for the current level
      this.subcategoryLevels[levelIndex] = { categories: subcategories };

      // Dynamically create form control for this level if not already created
      if (!this.addProductForm.contains(`subcategoryId${levelIndex}`)) {
        this.addProductForm.addControl(`subcategoryId${levelIndex}`, this.fb.control('', Validators.required));
      }

      // Check if this level has further subcategories and load them recursively
      subcategories.forEach(subcategory => {
        if ('subcategories' in subcategory && subcategory.subcategories?.length) {
          this.loadSubcategories(subcategory.subcategories, levelIndex + 1); // Recursive call for next level
        }
      });
    }
  }

  onSubcategoryChange(levelIndex: number): void {
    const selectedSubcategoryId: string | null = this.addProductForm.get(`subcategoryId${levelIndex}`)?.value;

    if (selectedSubcategoryId) {
      // Fetch the subcategory details for the selected subcategory
      this.categoryService.getCategoryById(selectedSubcategoryId).subscribe({
        next: (category: Category) => {
          // Set parent category when subcategory is selected
          this.addProductForm.patchValue({
            parentCategoryId: category.parentCategoryId || selectedSubcategoryId
          });

          // Update the subcategoryId form control with the selected value
          if (levelIndex === 0) {
            // Update the subcategoryId if this is the first level
            this.addProductForm.get('subcategoryId')?.setValue(selectedSubcategoryId);
          }

          // If the subcategory has further subcategories, load them
          if (category.subcategories?.length) {
            if (!this.subcategoryLevels[levelIndex + 1]) {
              this.subcategoryLevels.push({ categories: category.subcategories });
              this.addProductForm.addControl(`subcategoryId${levelIndex + 1}`, this.fb.control('', Validators.required));
            } else {
              this.subcategoryLevels[levelIndex + 1].categories = category.subcategories;
            }
          } else {
            this.subcategoryLevels.length = levelIndex + 1;
            // Remove higher levels if no further subcategories
          }
        },
        error: (error: HttpErrorResponse) => {
          console.error('Failed to load subcategories:', error);
        }
      });
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result; // Set image preview
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  onSubmit(): void {
    console.log(this.addProductForm.value, this.selectedFile);

    if (this.addProductForm.valid && this.selectedFile) {
      const formData = new FormData();
      formData.append('skuCode', this.addProductForm.get('skuCode')?.value);
      formData.append('name', this.addProductForm.get('name')?.value);
      formData.append('description', this.addProductForm.get('description')?.value);
      formData.append('price', this.addProductForm.get('price')?.value);
      formData.append('thumbnail', this.selectedFile, this.selectedFile.name);
      formData.append('isExclusive', this.addProductForm.get('isExclusive')?.value)

      const selectedCategories: string[] = [];

      selectedCategories.push(this.globalCategoryId || '');

      for (let i = 0; i < this.subcategoryLevels.length; i++) {
        const subcategoryId = this.addProductForm.get(`subcategoryId${i}`)?.value;
        if (subcategoryId) {
          selectedCategories.push(subcategoryId);
        }
      }

      // Add the array of selected subcategories to the form data
      formData.append('categories', JSON.stringify(selectedCategories));

      this.productService.createProduct(formData).subscribe({
        next: () => this.showMessage('success', 'Product Created Successfully!'),
        error: (error: HttpErrorResponse) => this.showMessage('error', 'Product creation failed: ' + error.message),
      });
    } else {
      this.showMessage('error', 'Please fill all required fields and select a thumbnail image.');
    }
  }

  get skuCode() {
    return this.addProductForm.get('skuCode');
  }

  get name() {
    return this.addProductForm.get('name');
  }

  get description() {
    return this.addProductForm.get('description');
  }

  get price() {
    return this.addProductForm.get('price');
  }

  showMessage(type: string, text: string) {
    this.animatedMessage = { type, text };
    setTimeout(() => { this.animatedMessage = null; }, 5000);
  }

  closeMessage() {
    this.animatedMessage = null;
  }

  get isExclusive() {
    return this.addProductForm.get('isExclusive')?.value === 'true'; // Return boolean value
  }
}
