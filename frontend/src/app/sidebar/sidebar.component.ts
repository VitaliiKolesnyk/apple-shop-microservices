import { Component, inject, OnInit } from '@angular/core';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { BehaviorSubject } from 'rxjs';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import {Category, Subcategory} from "../category";
import {CategoryService} from "../services/category.service";
import {ProductService} from "../services/product.service";

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  private readonly oidcSecurityService = inject(OidcSecurityService);
  userRoles: string[] = [];
  private rolesSubject = new BehaviorSubject<string[]>([]);
  categories: Category[] = [];
  categoryVisibilityMap: Map<string, boolean> = new Map();
  expandedCategories: { [key: string]: boolean } = {};// To store the IDs of expanded categories

  constructor(private categoryService: CategoryService,
              private productService: ProductService,
              private router: Router) { }

  ngOnInit(): void {
    console.log('SidebarComponent initialized');

    this.oidcSecurityService.isAuthenticated$.subscribe((isAuthenticated) => {
      if (isAuthenticated) {
        this.loadRoles();
      }
    });

    this.categoryService.getCategories().subscribe(categories => {
      // Filter categories to show only those where isMain = true
      this.categories = categories.filter(category => category.isMain);

      // Initialize all categories with subcategories hidden by default
      this.categories.forEach(category => {
        this.categoryVisibilityMap.set(category.id, false); // false = subcategories hidden
      });
    });
  }

  fetchCategories(): void {
    this.categoryService.getCategories().subscribe(categories => {
      this.categories = categories;
    });
  }

  toggleSubcategories(categoryId: string): void {
    this.expandedCategories[categoryId] = !this.expandedCategories[categoryId];
  }

  renderSubcategories(subcategories: Subcategory[]): any {
    return subcategories.map(subcategory => {
      return {
        ...subcategory,
        subcategories: this.renderSubcategories(subcategory.subcategories || [])
      };
    });
  }

  onCategoryClick(subcategory: Subcategory): void {
      this.productService.getProducts(0, 5, 'name', 'ASC', subcategory.id).subscribe((products) => {
        console.log(`Products fetched for subcategory ${subcategory.name}:`, products);
        this.router.navigateByUrl('/sidebar', { skipLocationChange: true }).then(() => {
          this.router.navigate(['/products'], { state: { products: products.content, categoryId: subcategory.id }, replaceUrl: true });
        });
      });
    }

  private loadRoles(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);

      if (decodedToken && decodedToken.realm_access) {
        this.userRoles = decodedToken.realm_access.roles || [];
        this.rolesSubject.next(this.userRoles);  // Update the roles subject
      }
    });
  }

  // Check if user has admin role
  hasAdminRole(): boolean {
    const hasRole = this.userRoles.includes('ROLE_ADMIN');
    return hasRole;
  }

  // Check if user has developer role
  hasDeveloperRole(): boolean {
    const hasRole = this.userRoles.includes('ROLE_DEVELOPER');
    return hasRole;
  }

  private decodeToken(token: string): any {
    try {
      console.log('Decoding token...');
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
          })
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Failed to decode token:', error);
      return null;
    }
  }
}
