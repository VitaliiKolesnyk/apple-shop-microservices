import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { Cart } from '../cart'; // Create a Cart model that matches your backend
import { CartItem } from '../cart';
import { Order } from '../order';

@Injectable({
  providedIn: 'root',
})
export class CartService {
  private cartPriceSource = new BehaviorSubject<number>(this.getSavedCartPrice());
  cartPrice$ = this.cartPriceSource.asObservable();

  private cartQuantitySource = new BehaviorSubject<number>(this.getSavedCartQuantity());
  cartQuantity$ = this.cartQuantitySource.asObservable();

  private baseUrl = 'http://localhost:9000/api/cart'; // Adjust the URL to your backend

  constructor(private http: HttpClient) {}

  public saveCartPrice(price: number): void {
    localStorage.setItem('cartPrice', price.toString()); // Persist to localStorage
  }

  public saveCartQuantity(quantity: number): void {
    localStorage.setItem('cartQuantity', quantity.toString()); // Persist to localStorage
  }

  private getSavedCartPrice(): number {
    const price = localStorage.getItem('cartPrice');
    return price ? parseFloat(price) : 0; // Parse price as a float value
  }

  private getSavedCartQuantity(): number {
    const quantity = localStorage.getItem('cartQuantity');
    return quantity ? parseInt(quantity) : 0;
  }

  updateCartPrice(price: number): void {
    this.cartPriceSource.next(price);
    this.saveCartPrice(price);
  }

  updateCartQuantity(quantity: number): void {
    this.cartQuantitySource.next(quantity);
    this.saveCartQuantity(quantity);
  }

  emitCartPriceChange(newPrice: number): void {
    this.updateCartPrice(newPrice); //
  }

  emitCartQuantityChange(newQuantity: number): void {
    this.updateCartQuantity(newQuantity);
  }

  // Add an item to the cart
  addToCart(userId: string, productId: string): Observable<Cart> {
    return this.http.post<Cart>(`${this.baseUrl}/add/${userId}/${productId}/1`, {});
  }

  // Get the user's cart (this should now include totalPrice)
  getCart(userId: string): Observable<Cart> {
    return this.http.get<Cart>(`${this.baseUrl}/${userId}`);
  }

  // Clear the user's cart
  clearCart(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/clear/${userId}`);
  }

  // Remove an item from the cart (this should return the updated cart with updated totalPrice)
  removeItemFromCart(userId: string, productId: string): Observable<Cart> {
    return this.http.delete<Cart>(`${this.baseUrl}/${userId}/items/${productId}`);
  }

  // Get all items in the user's cart
  getCartItems(userId: string): Observable<CartItem[]> {
    return this.http.get<CartItem[]>(`${this.baseUrl}/items/${userId}`); // Adjust the endpoint as necessary
  }

  // Confirm the order
  confirmOrder(order: Order, userId: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/confirm/${userId}`, order);
  }

  // Update the quantity of a cart item (this should return the updated cart with updated totalPrice)
  updateCartItemQuantity(userId: string, productId: string, quantity: number): Observable<Cart> {
    return this.http.put<Cart>(`${this.baseUrl}/${userId}/items/${productId}?newQuantity=${quantity}`, {});
  }

  getCartTotalPrice(userId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${userId}/totalPrice`); // Assuming your backend has an endpoint for this
  }

  getCartTotalQuantity(userId: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${userId}/totalQuantity`); // Assuming your backend has an endpoint for this
  }
}
