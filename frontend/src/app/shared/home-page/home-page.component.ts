import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {OidcSecurityService} from 'angular-auth-oidc-client';
import {Product} from '../../product';
import {ProductService} from '../../services/product.service';
import {ActivatedRoute, NavigationEnd, Router} from '@angular/router';
import {CartService} from '../../services/cart.service';
import {CategoryService} from '../../services/category.service';
import {Category} from '../../category';
import {CommonModule} from '@angular/common';
import {ProductPage} from '../../product';
import {AsyncPipe, JsonPipe} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {EmailService} from "../../services/email.service";
import {filter, Subscription} from "rxjs";


interface ProductsState {
  products: Array<Product>;
  categoryId: string;
}

@Component({
  selector: 'app-homepage',
  templateUrl: './home-page.component.html',
  standalone: true,
  imports: [AsyncPipe, JsonPipe, FormsModule, CommonModule, RouterModule],
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent implements OnInit {
  private readonly oidcSecurityService = inject(OidcSecurityService);
  private readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);
  private readonly categoryService = inject(CategoryService);

  isAuthenticated = false;
  products: Array<Product> = [];
  orderSuccess = false;
  errorMessage = '';
  successMessage = '';
  userRoles: string[] = [];
  currentPage = 1;
  totalPages = 1;
  userId = '';
  selectedSubcategory = '';
  categories: Category[] = [];
  searchTerm: string = '';
  animatedMessage: { type: string; text: string } | null = null;

  pageSize = 10; // Default page size
  itemsPerPageOptions = [5, 10, 15, 20];
  sortField = 'name';  // Default sorting field
  sortDir = 'ASC';     // Default sorting direction
  productPage!: ProductPage;

  currentView: 'list' | 'grid' | 'three-column' | 'five-column' = 'grid';

  pageNumbers: number[] = [];

  constructor(private router: Router) {
  }

  ngOnInit(): void {
    // Subscribe to route changes and trigger refresh logic on NavigationEnd
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.checkAndUpdateProductsState();
    });

    // Initial load
    this.checkAndUpdateProductsState();

    // Authentication logic
    this.oidcSecurityService.isAuthenticated$.subscribe(({isAuthenticated}) => {
      console.log('User isAuthenticated: ', isAuthenticated);
      this.isAuthenticated = isAuthenticated;

      if (isAuthenticated) {
        this.loadRoles();
        this.loadUserId();
      }

      this.loadCategories();
    });
  }

  private checkAndUpdateProductsState(): void {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras.state as ProductsState || history.state;

    if (state && 'products' in state) {
      // If state has products (including an empty array), use them
      console.log('Received products state:', state.products);
      this.products = state.products || [];
      this.selectedSubcategory = state.categoryId;
    } else {
      // If no state or no products in state, load products from the backend
      console.log('No products state found, loading from backend.');
      this.loadProducts(this.currentPage, this.pageSize, this.sortField, this.sortDir);
    }

    console.log('Final products in the component:', this.products);
  }

  private loadRoles(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.realm_access) {
        this.userRoles = decodedToken.realm_access.roles || [];
      }
    });
  }

  loadProducts(page: number, size: number = this.pageSize, sortField: string = this.sortField, sortDirection: string = this.sortDir, selectedSubcategory = this.selectedSubcategory, search = this.searchTerm): void {
    this.productService.getProducts(page - 1, size, sortField, sortDirection, selectedSubcategory, search).subscribe(
      (response) => {
        this.productPage = response;
        this.products = response.content;
        this.currentPage = response.number + 1; // Adjust back to 1-based for UI
        this.totalPages = response.totalPages;

        // Update pageNumbers based on totalPages
        this.pageNumbers = Array.from({length: this.totalPages}, (_, i) => i + 1);
      },
      (error) => {
        console.error('Error loading products', error);
      }
    );
  }

  // New method to set the view
  setView(view: 'list' | 'grid' | 'three-column' | 'five-column') {
    this.currentView = view;
  }

  onItemsPerPageChange(newSize: number): void {
    this.pageSize = newSize;
    this.currentPage = 1; // Reset to the first page
    this.loadProducts(this.currentPage, this.pageSize);
  }

  onCategoryChange(category: string): void {
    this.selectedSubcategory = category
    this.currentPage = 1;
    this.loadProducts(this.currentPage, this.pageSize, this.sortField, this.sortDir, this.selectedSubcategory);
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

  goToCreateProductPage(): void {
    this.router.navigateByUrl('/add-product');
  }

  removeProduct(productId: string | undefined): void {
    if (productId) {
      this.productService.removeProduct(productId).subscribe({
        next: () => {
          this.products = this.products.filter((product) => product.id !== productId);
        },
        error: (error) => {
          this.errorMessage = 'You do not have the rights to remove products.';
        },
      });
    } else {
      this.errorMessage = 'Product ID is missing';
    }
  }

  hasAdminRole(): boolean {
    return this.userRoles.includes('ROLE_ADMIN');
  }

  hasUserRole(): boolean {
    return this.userRoles.includes('ROLE_USER');
  }

  addToCart(productId: string | undefined): void {
    if (!this.userId || !productId) {
      this.errorMessage = 'Please provide a valid user ID, product ID';
      return;
    }

    this.cartService.addToCart(this.userId, productId).subscribe(
      (cart) => {
        this.successMessage = 'Product added to cart successfully!';
        this.errorMessage = '';

        // Update total price in cart after adding product
        const totalPrice = cart.totalPrice || 0;
        const totalQuantity = cart.cartItems?.reduce((acc, item) => acc + item.quantity, 0);
        this.cartService.updateCartPrice(totalPrice);
        // @ts-ignore
        this.cartService.updateCartQuantity(totalQuantity)
      },
      (error) => {
        this.errorMessage = 'Failed to add product to cart.';
      }
    );
  }

  private loadUserId(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.sub) {
        this.userId = decodedToken.sub;
      }
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

  onSearch(): void {
    this.loadProducts(1, this.pageSize, this.sortField, this.sortDir, this.selectedSubcategory, this.searchTerm);
  }

}
