import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { OrderService } from '../services/order.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import {OidcSecurityService} from "angular-auth-oidc-client";

@Component({
  selector: 'app-order-details',
  templateUrl: './order-details.component.html',
  standalone: true,
  imports: [CommonModule]
})
export class OrderDetailsComponent implements OnInit {
  order: any;
  isAuthenticated = false;
  userId = '';
  userEmail: string = ''
  userRoles: string[] = [];

  constructor(
    private route: ActivatedRoute,
    private orderService: OrderService,
    private oidcSecurityService: OidcSecurityService,
    private router: Router,
  ) {}

  ngOnInit() {
    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;
      if (isAuthenticated) {
        this.loadUserEmail();
        this.loadUserId();
        this.loadRoles();
      }
    });

    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      this.fetchOrderDetails(orderId);
    }
  }

  fetchOrderDetails(orderId: string) {
    this.orderService.getOrderById(orderId).subscribe(
      (order) => {
        this.order = order;
        console.log('Fetched order:', this.order);
      },
      (error) => {
        console.error('Error fetching order:', error);
      }
    );
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

  private loadUserId(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.sub) {
        this.userId = decodedToken.sub;
      }
    });
  }

  private loadUserEmail(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.email) {
        this.userEmail = decodedToken.email;
      }
    });
  }

  private loadRoles(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.realm_access) {
        this.userRoles = decodedToken.realm_access.roles || [];
      }
    });
  }

  hasUserRole(): boolean {
    return this.userRoles.includes('ROLE_USER');
  }

  goToPayment() {
    this.router.navigate(['/payment'], { state: { amount: this.order.totalAmount, userId: this.userId, userEmail: this.userEmail, orderNumber: this.order.orderNumber } });
  }
}
