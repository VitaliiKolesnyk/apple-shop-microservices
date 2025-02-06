import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService } from '../services/product.service';
import { Product } from '../product';
import { CartService } from '../services/cart.service';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router} from "@angular/router";
import {CommentService} from "../services/comment.service";
import {CommentDto} from "../comment-dto";
import {CommentRequest} from "../comment.request";
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-product-details',
  templateUrl: './product-details.component.html',
  styleUrls: ['./product-details.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class ProductDetailsComponent implements OnInit {
  product: Product | undefined;
  errorMessage = '';
  userId = '';
  userName = '';
  successMessage = '';
  isAuthenticated = false;
  selectedFile: File | null = null;
  userRoles: string[] = [];
  comments: CommentDto[] = [];
  newCommentText = '';
  replyText = '';
  commentsVisible: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private cartService: CartService,
    private commentService: CommentService,
    private oidcSecurityService: OidcSecurityService,
    private router: Router,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const productId = params.get('id');
      if (productId) {
        this.getProductById(productId);
        this.getComments(productId);
      }
    });

    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;
      if (isAuthenticated) {
        this.loadRoles()
        this.loadUserId();
        this.loadUserName();
      }
    });
  }

  getComments(productId: string): void {
    this.commentService.getComments(productId).subscribe({
      next: (comments) => this.comments = comments,
      error: (err) => this.errorMessage = 'Failed to load comments'
    });
  }

  addComment(): void {
    // Check if comment text is empty after trimming whitespace
    if (!this.newCommentText.trim()) {
      return;
    }

    // Ensure product and product id are defined
    if (!this.product || !this.product.id) {
      this.errorMessage = 'Product not found';
      return;
    }

    const commentRequest: CommentRequest = {
      text: this.newCommentText,
      author: this.userName || 'Anonymous',
    };

    // Call the service to add the comment
    this.commentService.addComment(this.product.id, commentRequest).subscribe({
      next: (comment) => {
        // @ts-ignore
        this.getComments(this.product.id);
        this.newCommentText = '';
      },
      error: (err) => {
        this.errorMessage = 'Failed to add comment';
        console.error(err);
      }
    });
  }

  addReply(commentId: string): void {
    // Ensure the reply text is not empty
    if (!this.replyText.trim()) return;

    // Ensure the product and its id are defined
    if (!this.product || !this.product.id) {
      this.errorMessage = 'Product not found';
      return;
    }

    // Create the reply DTO (with the required properties)
    const commentRequest: CommentRequest = {
      author: this.userName,
      text: this.replyText,
    };

    // Call the service to add the reply
    this.commentService.addReply(this.product.id, commentId, commentRequest).subscribe({
      next: (reply) => {
        // @ts-ignore
        this.getComments(this.product.id);
        this.replyText = '';
      },
      error: (err) => {
        this.errorMessage = 'Failed to add reply';
        console.error(err);
      }
    });
  }

  deleteComment(commentId: string | undefined): void {
    // Check if commentId is defined and if product id is valid
    if (!commentId || !this.product?.id) {
      this.errorMessage = 'Invalid comment ID or product ID';
      return;
    }

    this.commentService.deleteComment(this.product.id, commentId).subscribe({
      next: () => {
        // Filter out the deleted comment
        this.comments = this.comments.filter(c => c.id !== commentId);
      },
      error: (err) => {
        this.errorMessage = 'Failed to delete comment';
        console.error(err); // Log the error for debugging
      }
    });
  }


  getProductById(productId: string): void {
    this.productService.getProductById(productId).subscribe({
      next: (product) => this.product = product,
      error: (err) => this.errorMessage = 'Product not found'
    });
  }

  addToCart(productId: string | undefined): void {
    if (!this.userId || !productId) {
      this.errorMessage = 'Please provide a valid user ID, product ID, and quantity.';
      return;
    }

    this.cartService.addToCart(this.userId, productId).subscribe(
      () => {
        this.successMessage = 'Product added to cart successfully!';
        this.errorMessage = '';

        // Clear success message after 5 seconds
        setTimeout(() => {
          this.successMessage = '';
        }, 5000);
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

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  updateProduct(productId: string | undefined): void {
    if (!productId) {
      this.errorMessage = 'Product ID is missing';
      return;
    }

    if (!this.product) return;

    const formData = new FormData();

    // Append the product fields to FormData
    formData.append('name', this.product.name || '');
    formData.append('description', this.product.description || '');
    formData.append('price', this.product.price?.toString() || '');

    // Append the file to FormData if one is selected
    if (this.selectedFile) {
      formData.append('thumbnail', this.selectedFile);
    }

    // Send the formData to the backend
    this.productService.updateProduct(productId, formData).subscribe({
      next: () => {
        this.successMessage = 'Product updated successfully!'
        this.getProductById(productId)
      },
      error: (err) => this.errorMessage = 'Failed to update product'
    });
  }

  hasAdminRole(): boolean {
    return this.userRoles.includes('ROLE_ADMIN');
  }

  private loadRoles(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.realm_access) {
        this.userRoles = decodedToken.realm_access.roles || [];
        console.log('User Roles:', this.userRoles);
      }
    });
  }

  private loadUserName(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.email) {
        this.userName = decodedToken.name;
      }
    });
  }

  trackByCommentId(index: number, comment: CommentDto): string {
    return comment.id;
  }

  toggleComments(): void {
    this.commentsVisible = !this.commentsVisible;
  }
}
