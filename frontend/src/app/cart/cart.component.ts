import { Component, OnInit } from '@angular/core';
import { CartService } from '../services/cart.service';
import { Cart } from '../cart';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { CommonModule } from '@angular/common';
import {ContactDetails, Order, OrderResponse } from '../order';
import { OrderService } from '../services/order.service';
import { ProducDto } from '../order';
import { UserDetails } from '../order';
import { FormGroup, FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { countries } from '../data/countries';
import { CountryCode, countryCodes } from "../data/phone-codes";


@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class CartComponent implements OnInit {
  cart: Cart | null = null;
  orderSuccess = false;
  orderFailed = false;
  errorMessage: string = '';
  successMessage: string = '';
  showCheckoutForm: boolean = false;
  userId: string = '';
  userEmail: string = '';
  orderNumber: string | undefined = '';
  order: Order | null = null;
  countries: string[] = countries;
  countryCodes: CountryCode[] = countryCodes;

  checkoutDetails = {
    name: '',
    surname: '',
    email: '',
    phone: '',
    countryCode: '',
    country: '',
    city: '',
    street: ''
  };

  constructor(private cartService: CartService, private oidcSecurityService: OidcSecurityService,
    private orderService: OrderService, private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUserCart();
  }

  triggerValidation(form: NgForm): void {
    Object.keys(form.controls).forEach((field) => {
      const control = form.controls[field];
      control.markAsTouched({ onlySelf: true });
    });
  }

  private loadUserCart(): void {
    this.oidcSecurityService.getAccessToken().subscribe(
      (token) => {
        const userId =  this.decodeToken(token);
        this.userId = userId ?? '';

        if (userId) {
          this.loadCart(userId);
        } else {
          console.error('User ID not found in token');
        }
      },
      (error) => {
        console.error('Failed to get access token:', error);
      }
    );
  }

  private loadCart(userId: string): void {
    this.cartService.getCart(userId).subscribe(
      (cart) => {
        this.cart = cart;

        if (this.cart) {
          const totalPrice = this.cart.totalPrice ?? 0;
          const totalQuantity = cart.cartItems?.reduce((acc, item) => acc + item.quantity, 0);

          console.log('totalQuantity - ', totalQuantity);
          this.cartService.emitCartPriceChange(totalPrice);
          this.cartService.saveCartPrice(totalPrice);

          // @ts-ignore
          this.cartService.emitCartQuantityChange(totalQuantity);
          // @ts-ignore
          this.cartService.saveCartQuantity(totalQuantity);
        }
      },
      (error) => {
        console.error('Failed to load cart:', error);
      }
    );
  }

  clearCart(): void {
    this.oidcSecurityService.getAccessToken().subscribe(
      (token) => {
        const userId = this.decodeToken(token);
        if (userId) {
          this.cartService.clearCart(userId).subscribe(
            () => {
              this.loadCart(userId);
            },
            (error) => {
              console.error('Failed to clear cart:', error);
            }
          );
        }
      },
      (error) => {
        console.error('Failed to get access token:', error);
      }
    );
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
      return decoded.sub || decoded.userId || null;
    } catch (error) {
      console.error('Failed to decode token:', error);
      return null;
    }
  }

  removeItem(productId: string): void {
    this.oidcSecurityService.getAccessToken().subscribe(
      (token) => {
        const userId = this.decodeToken(token);
        if (userId) {
          this.cartService.removeItemFromCart(userId, productId).subscribe(
            (updatedCart: Cart) => {
              this.cart = updatedCart;

              const totalPrice = updatedCart?.totalPrice ?? 0;

              this.cartService.emitCartPriceChange(totalPrice);

              this.cartService.saveCartPrice(totalPrice);

              const totalQuantity = updatedCart.cartItems?.reduce((acc, item) => acc + item.quantity, 0);
              console.log('totalQuantity - ', totalQuantity);
              // @ts-ignore
              this.cartService.emitCartQuantityChange(totalQuantity);
              // @ts-ignore
              this.cartService.saveCartQuantity(totalQuantity);

              this.loadCart(userId);
            },
            (error) => {
              console.error('Failed to remove item from cart:', error);
            }
          );
        }
      },
      (error) => {
        console.error('Failed to get access token:', error);
      }
    );
  }


  orderProduct() {
    console.log('Order button clicked');

    // Get the user data from the OIDC Security Service
    this.oidcSecurityService.userData$.subscribe(result => {
      const userDetails: UserDetails = {
        email: result.userData.email,
        firstName: result.userData.given_name,
        lastName: result.userData.family_name
      };

      this.userEmail = userDetails.email;

      const contactDetails: ContactDetails = {
        name: '',
        surname: '',
        email: '',
        phone: '',
        country: '',
        city: '',
        street: ''
      };

      // Fetch cart using user ID and the token
      this.oidcSecurityService.getAccessToken().subscribe(token => {
        const userId = this.decodeToken(token); // Assuming decodeToken extracts userId from JWT token
        if (userId) {
          this.cartService.getCart(userId).subscribe(cart => {
            if (!cart || !cart.cartItems || cart.cartItems.length === 0) {
              console.log('Cart is empty or undefined!');
              return;
            }

            const totalQuantity = cart.cartItems.reduce((sum, item) => sum + item.quantity, 0);

            const products: ProducDto[] = cart.cartItems.map(item => ({
              name: item.product.name,
              skuCode: item.product.skuCode,
              price: item.product.price,
              quantity: item.quantity,
              totalAmount: item.totalPrice,
              thumbnailUrl: item.thumbnailUrl
            }));

            this.order = {
              userId: userId,
              quantity: totalQuantity,
              userDetails: userDetails,
              products: products,
              contactDetails: contactDetails,
            };

            this.showCheckoutForm = true;
          });
        }
      });
    });
  }

  confirmOrder(form: NgForm): void {
    if (form.valid) {
      if (this.order && this.checkoutDetails) {

        const orderWithContactDetails: Order = {
          ...this.order,
          contactDetails: {
            name: this.checkoutDetails.name,
            surname: this.checkoutDetails.surname,
            email: this.checkoutDetails.email,
            phone: this.updatePhoneWithCountryCode(),
            country: this.checkoutDetails.country,
            city: this.checkoutDetails.city,
            street: this.checkoutDetails.street
          }
        };

        this.orderService.orderProduct(orderWithContactDetails).subscribe({
          next: (orderResponse) => {
            this.orderSuccess = true;
            this.orderFailed = false;
            this.successMessage = 'Order confirmed successfully!';
            this.clearCart();
            this.showCheckoutForm = false;
            this.orderNumber = orderResponse.orderNumber;
            this.goToPayment();
          },
          error: (error) => {
            this.orderFailed = true;
            this.orderSuccess = false;
            this.errorMessage = error.message || 'Failed to place order. Please try again.';
            console.error('Order failed:', error);
          }
        });
      }
      console.log('Form is valid, confirming order...');
    } else {
      console.log('Form is invalid, please fix the errors.');
    }
  }

  goToPayment() {
    console.log('Sku codes is - ' + this.getSkuCodes(this.cart));
    this.router.navigate(['/payment'], { state: { amount: this.cart?.totalPrice, userId: this.userId,
        userEmail: this.userEmail, orderNumber: this.orderNumber, skuCodes: this.getSkuCodes(this.cart) } });
  }

  getSkuCodes(cart: Cart | null): string[] {
    // @ts-ignore
    return cart.cartItems?.map(item => item.product.skuCode) || [];
  }

  updateItemQuantity(productId: string, newQuantity: number): void {
    if (newQuantity > 0) {
      this.oidcSecurityService.getAccessToken().subscribe(
        (token) => {
          const userId = this.decodeToken(token);
          if (userId) {
            this.cartService.updateCartItemQuantity(userId, productId, newQuantity).subscribe(
              (updatedCart) => {
                this.cart = updatedCart;

                const totalPrice = updatedCart?.totalPrice ?? 0;

                this.cartService.emitCartPriceChange(totalPrice);

                const totalQuantity = updatedCart.cartItems?.reduce((acc, item) => acc + item.quantity, 0);
                console.log('totalQuantity - ', totalQuantity);
                // @ts-ignore
                this.cartService.emitCartQuantityChange(totalQuantity);
                // @ts-ignore
                this.cartService.saveCartQuantity(totalQuantity);
              },
              (error) => {
                console.error('Failed to update item quantity:', error);
              }
            );
          }
        },
        (error) => {
          console.error('Failed to get access token:', error);
        }
      );
    }
  }

  updatePhoneWithCountryCode(): string {
    const countryCode = this.checkoutDetails.countryCode;
    const phone = this.checkoutDetails.phone;

    if (countryCode && phone) {
      return `${countryCode}${phone}`;
    }

    return ''
  }

  changeQuantity(item: any, delta: number) {
    item.quantity = Math.max(1, item.quantity + delta); // Prevent quantity from going below 1
    this.updateItemQuantity(item.product.id!, item.quantity);
  }
}
