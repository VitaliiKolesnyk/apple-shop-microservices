import { Injectable } from '@angular/core';
import { Product } from '../product';
import {Observable, throwError} from "rxjs";
import {HttpClient, HttpErrorResponse, HttpHeaders} from "@angular/common/http";
import {Order, OrderResponse} from '../order';
import { OrderDto } from '../order-dto';
import { StripeChargeDto } from '../order';
import {catchError} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  constructor(private httpClient: HttpClient) {
  }

  orderProduct(order: Order): Observable<OrderResponse> {
    return this.httpClient.post<OrderResponse>('http://localhost:9000/api/orders', order).pipe(
      catchError(this.handleError)
    );
  }

  getOrders(orderNumber: string): Observable<OrderDto[]> {
    return this.httpClient.get<OrderDto[]>(`http://localhost:9000/api/orders?orderNumber=${orderNumber}`);
  }

  updateOrderStatus(order: OrderDto): Observable<Order> {
    return this.httpClient.put<Order>(`http://localhost:9000/api/orders/${order.id}`, order, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  deleteOrder(orderId: number): Observable<void> {
    return this.httpClient.delete<void>(`http://localhost:9000/api/orders/${orderId}`);
  }

  getOrderById(id: string): Observable<any> { // Replace `any` with your actual order type
    return this.httpClient.get<any>(`http://localhost:9000/api/orders/${id}`);
  }

  getOrdersByUserId(userId: string): Observable<OrderDto[]> {
    const url = `http://localhost:9000/api/orders/user/${userId}`;  // Include userId in the URL
    return this.httpClient.get<OrderDto[]>(url);
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unexpected error occurred.';
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = error.error.message || 'An unexpected server error occurred.';
    }
    return throwError(() => new Error(errorMessage));
  }
}
