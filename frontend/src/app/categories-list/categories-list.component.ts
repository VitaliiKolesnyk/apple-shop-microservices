import { Component, OnInit } from '@angular/core';
import { Category } from '../category';
import { CategoryService } from '../services/category.service';
import { Router } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-category-list',
  templateUrl: './categories-list.component.html',
  styleUrls: ['./categories-list.component.css'],
  standalone: true,
  imports: [FormsModule, CommonModule, RouterModule]
})
export class CategoryListComponent implements OnInit {
  categories: Category[] = [];
  errorMessage: string | null = null;
  userRoles: string[] = [];
  isAuthenticated = false;

  constructor(private categoryService: CategoryService, private router: Router, private oidcSecurityService: OidcSecurityService) {}

  ngOnInit(): void {
    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;
      if (isAuthenticated) {
        this.loadRoles();
      }
      this.loadCategories();  // Always load categories, even for unauthenticated users
    });
  }

  private loadCategories(): void {
    this.categoryService.getCategories().subscribe(
      (categories) => {
        this.categories = categories.filter(category => category.isMain === true);
      },
      (error) => {
        console.error('Error loading categories', error);
      }
    );
  }

  deleteCategory(id: string): void {
    if (confirm('Are you sure you want to delete this category?')) {
      this.categoryService.deleteCategory(id).subscribe(
        () => {
          this.categories = this.categories.filter(category => category.id !== id);
        },
        (error) => {
          this.errorMessage = 'Failed to delete category.';
        }
      );
    }
  }

  goToCreateCategoryPage(): void {
    this.router.navigateByUrl('/categories');
  }

  hasAdminRole(): boolean {
    return this.userRoles.includes('ROLE_ADMIN');
  }

  private loadRoles(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.realm_access) {
        this.userRoles = decodedToken.realm_access.roles || [];
      }
    });
  }

  private decodeToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`)
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Failed to decode token:', error);
      return null;
    }
  }
}
