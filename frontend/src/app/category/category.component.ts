import {Component, inject, OnInit} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CategoryService } from '../services/category.service';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import {HttpErrorResponse} from "@angular/common/http";
import {Category} from "../category";

@Component({
  selector: 'app-add-category',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.css']
})
export class CategoryComponent implements OnInit {
  addCategoryForm: FormGroup;
  successMessage = '';
  errorMessage = '';
  selectedFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = '';
  categories: Category[] = [];
  subcategoryLevels: any[] = [];

  constructor(private fb: FormBuilder, private categoryService: CategoryService) {
    this.addCategoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      parentCategoryId: [''],
      subcategoryId: ['']
    });
  }

  ngOnInit(): void {
    this.addCategoryForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      parentCategoryId: [''], // Add the form control for parent category
      subcategoryId: [''] // Initialize subcategoryId
    });

    this.categoryService.getCategories().subscribe({
      next: (categories) => {
        this.categories = categories; // Populate the parent category dropdown
      },
      error: (error: HttpErrorResponse) => {
        console.error('Failed to load categories:', error);
      }
    });
  }

  onSubmit(): void {
    if (this.addCategoryForm.valid) {
      const formData = new FormData();
      formData.append('name', this.addCategoryForm.get('name')?.value);

      // Append the selected parent category if provided
      const parentCategory = this.addCategoryForm.get('parentCategoryId')?.value;
      if (parentCategory) {
        formData.append('parentCategoryId', parentCategory);
      }

      if (this.selectedFile) {
        formData.append('thumbnail', this.selectedFile, this.selectedFile.name);
      }

      this.categoryService.addCategory(formData).subscribe({
        next: () => {
          this.successMessage = 'Category added successfully!';
          this.addCategoryForm.reset();
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage = error.message;
        }
      });
    }
  }

  onParentCategoryChange(): void {
    const parentCategoryId = this.addCategoryForm.get('parentCategoryId')?.value;

    if (parentCategoryId) {
      this.categoryService.getCategoryById(parentCategoryId).subscribe({
        next: (category: Category) => {
          this.subcategoryLevels = [{ parentCategoryId, categories: category.subcategories || [] }];
          this.addCategoryForm.addControl('subcategoryId0', this.fb.control(''));
        },
        error: (error: HttpErrorResponse) => {
          console.error('Failed to load subcategories:', error);
        }
      });
    }
  }

  onSubcategoryChange(levelIndex: number): void {
    const subcategoryId = this.addCategoryForm.get(`subcategoryId${levelIndex}`)?.value;

    if (subcategoryId) {
      this.categoryService.getCategoryById(subcategoryId).subscribe({
        next: (category: Category) => {
          // Set the parent category ID when a subcategory is selected
          this.addCategoryForm.patchValue({
            parentCategoryId: category.parentCategoryId || subcategoryId
          });

          // If subcategories exist, load the next level of subcategories
          if (category.subcategories?.length) {
            if (this.subcategoryLevels[levelIndex + 1]) {
              this.subcategoryLevels[levelIndex + 1].categories = category.subcategories;
            } else {
              this.subcategoryLevels.push({ parentCategoryId: subcategoryId, categories: category.subcategories });
              this.addCategoryForm.addControl(`subcategoryId${levelIndex + 1}`, this.fb.control(''));
            }
          } else {
            this.subcategoryLevels.length = levelIndex + 1;
          }
        },
        error: (error: HttpErrorResponse) => {
          console.error('Failed to load subcategories:', error);
        }
      });
    }
  }

  onFileSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result;
      };
      reader.readAsDataURL(file);
    }
  }
}
