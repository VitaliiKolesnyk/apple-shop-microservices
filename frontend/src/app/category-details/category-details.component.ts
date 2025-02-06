import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Category } from "../category";
import { CategoryService } from "../services/category.service";
import { Router } from "@angular/router";

@Component({
  selector: 'app-category-details',
  templateUrl: './category-details.component.html',
  styleUrls: ['./category-details.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class CategoryDetailsComponent implements OnInit {
  category: Category | null = null;  // Initialize to null instead of undefined
  errorMessage = '';
  successMessage = '';
  isAuthenticated = false;
  selectedFile: File | null = null;

  constructor(
    private route: ActivatedRoute,
    private categoryService: CategoryService,
    private oidcSecurityService: OidcSecurityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;
      if (isAuthenticated) {
        this.route.paramMap.subscribe(params => {
          const categoryId = params.get('id');
          if (categoryId) {
            this.getCategoryById(categoryId);
          }
        });
      }
    });
  }

  getCategoryById(categoryId: string): void {
    this.categoryService.getCategoryById(categoryId).subscribe({
      next: (category) => {
        console.log('Fetched category:', category);  // Log category to inspect its structure
        this.category = category;
      },
      error: (err) => this.errorMessage = 'Category not found'
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  updateCategory(categoryId: string): void {
    if (!this.category) return;

    const formData = new FormData();
    formData.append('name', this.category.name || '');

    if (this.selectedFile) {
      formData.append('thumbnail', this.selectedFile);
    }

    this.categoryService.updateCategory(categoryId, formData).subscribe({
      next: () => {
        this.successMessage = 'Category updated successfully!';
        this.getCategoryById(categoryId);
      },
      error: (err) => {
        this.errorMessage = 'Failed to update category';
      }
    });
  }

  deleteCategory(categoryId: string | undefined): void {
    if (!categoryId) {
      this.errorMessage = 'Category ID is missing';
      return;
    }

    if (confirm('Are you sure you want to delete this category?')) {
      this.categoryService.deleteCategory(categoryId).subscribe({
        next: () => {
          this.successMessage = 'Category deleted successfully!';
          if (this.category) {
            this.getCategoryById(this.category.id);
          }
        },
        error: () => {
          this.errorMessage = 'Failed to delete category';
        }
      });
    }
  }
}
