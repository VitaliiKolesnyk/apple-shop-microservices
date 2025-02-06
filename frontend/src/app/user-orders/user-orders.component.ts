import { Component, OnInit, inject } from '@angular/core';
import { OrderService } from '../services/order.service';
import { OrderDto } from '../order-dto';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-user-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-orders.component.html',
  styleUrl: './user-orders.component.css'
})
export class UserOrdersComponent {
  orders: OrderDto[] = [];
  errorMessage: string = '';
  selectedStatus: string = '';
  filteredOrders: OrderDto[] = [];
  paginatedOrders: OrderDto[] = [];
  currentPage: number = 1;
  pageSize: number = 5;  // Number of orders per page
  totalPages: number = 1;

  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);
  private readonly oidcSecurityService = inject(OidcSecurityService);

  ngOnInit(): void {
    this.loadUserCart();
  }

  private loadUserCart(): void {
    this.oidcSecurityService.getAccessToken().subscribe(
      (token) => {
        const userId = this.decodeToken(token);  // Get the current user ID
      this.orderService.getOrdersByUserId(userId!).subscribe({
        next: (orders) => {
          console.log('Orders fetched:', orders);
          this.orders = orders;
          this.filterOrders();
        },
        error: (error) => {
          this.errorMessage = 'Failed to load orders. Please try again later.';
          console.error('Error fetching orders:', error);
        }
      });
      },
      (error) => {
        console.error('Failed to get access token:', error);
      }
    );
  }

  cancellOrder(order: OrderDto): void {
    const updatedOrder = { ...order, status: 'CANCELLED' };  // Assign the new status
    this.orderService.updateOrderStatus(updatedOrder).subscribe({
      next: () => {
        alert('Order was cancelled');
        
        // Update the status in the local orders array
        const index = this.orders.findIndex(o => o.id === order.id);
        if (index !== -1) {
          this.orders[index].status = 'CANCELLED';
        }
  
        // Reapply filters and pagination to reflect the updated status
        this.filterOrders();
      },
      error: (error) => {
        console.error('Failed to cancell order:', error);
        this.errorMessage = 'Failed to cancell order.';
      }
    });
  }  
  
  filterOrders() {
    if (!this.selectedStatus) {
      this.filteredOrders = this.orders;  // Show all orders if no filter is selected
    } else {
      this.filteredOrders = this.orders.filter(order => order.status === this.selectedStatus);
    }
    this.paginateOrders();  // Re-apply pagination whenever the filter changes
  }

  paginateOrders() {
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    this.paginatedOrders = this.filteredOrders.slice(start, end);
    this.totalPages = Math.ceil(this.filteredOrders.length / this.pageSize);  // Calculate the total pages
  }

  // Pagination navigation functions
  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.paginateOrders();  // Re-apply pagination on page change
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.paginateOrders();  // Re-apply pagination on page change
    }
  }

  openOrderDetails(orderId: number) {
    this.router.navigate(['/order-details', orderId]); // Adjust the route as necessary
  }

  private decodeToken(token: string): string | null {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );

      const decoded = JSON.parse(jsonPayload);
      return decoded.sub || decoded.userId || null; // Adjust according to your JWT structure
    } catch (error) {
      console.error('Failed to decode token:', error);
      return null;
    }
  }
}
