import { Component, OnInit, inject } from '@angular/core';
import { OrderService } from '../services/order.service';
import { OrderDto } from '../order-dto';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
  styleUrls: ['./order-list.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class OrdersListComponent implements OnInit {
  orders: OrderDto[] = [];
  errorMessage: string = '';
  selectedStatus: string = '';
  filteredOrders: OrderDto[] = [];
  paginatedOrders: OrderDto[] = [];
  currentPage: number = 1;
  pageSize: number = 5;  // Number of orders per page
  totalPages: number = 1;
  startDate?: Date;  // Start date for filtering
  endDate?: Date;
  orderNumber: string = '';

  private readonly orderService = inject(OrderService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.orderService.getOrders(this.orderNumber).subscribe({
      next: (orders) => {
        this.orders = orders;
        this.filterOrders();
      },
      error: (error) => {
        this.errorMessage = 'Failed to load orders. Please try again later.';
        console.error('Error fetching orders:', error);
      }
    });
  }

  searchOrderNumber(): void {
    if (this.orderNumber.trim()) {
      this.loadOrders();
    } else {
      this.errorMessage = 'Order number not found';
    }
  }

  updateOrderStatus(order: OrderDto): void {
    const updatedOrder = { ...order, status: order.newStatus };  // Assign the new status
    this.orderService.updateOrderStatus(updatedOrder).subscribe({
      next: () => {
        alert('Order status updated successfully');

        // Update the status in the local orders array
        const orderIndex = this.orders.findIndex(o => o.id === updatedOrder.id);
        if (orderIndex > -1) {
          this.orders[orderIndex].status = updatedOrder.status;  // Update the status
        }

        // Re-filter and re-paginate to reflect the changes
        this.filterOrders();
      },
      error: (error) => {
        console.error('Failed to update order status:', error);
        this.errorMessage = 'Failed to update order status.';
      }
    });
  }

  deleteOrder(orderId: number): void {
    this.orderService.deleteOrder(orderId).subscribe({
      next: () => {
        // Remove the deleted order from the list
        this.orders = this.orders.filter(order => order.id !== orderId);
        this.filterOrders();
        alert('Order deleted successfully');
      },
      error: (error) => {
        console.error('Failed to delete order:', error);
        this.errorMessage = 'Failed to delete order.';
      }
    });
  }

  filterOrders() {
    this.filteredOrders = this.orders;

    // Filter by status if selected
    if (this.selectedStatus) {
      this.filteredOrders = this.filteredOrders.filter(order => order.status === this.selectedStatus);
    }

    // Filter by orderedAt date range if both dates are provided
    if (this.startDate && this.endDate) {
      const start = new Date(this.startDate).getTime();
      const end = new Date(this.endDate).getTime();
      this.filteredOrders = this.filteredOrders.filter(order => {
        const orderDate = new Date(order.orderedAt).getTime();
        return orderDate >= start && orderDate <= end;
      });
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

  statusClasses(status: string) {
    return {
      'bg-green-600 text-white': status === 'DELIVERED',
      'bg-green-600 text-blue': status === 'PAID',
      'bg-yellow-500 text-white': status === 'NEW',
      'bg-red-500 text-white': status === 'CANCELLED',
    };
  }
}
