import {Component, inject, OnInit} from '@angular/core';
import {OidcSecurityService} from "angular-auth-oidc-client";
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { RouterModule } from '@angular/router';
import {EmailService} from "../../services/email.service";

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {

  private readonly oidcSecurityService = inject(OidcSecurityService);
  private readonly router = inject(Router);
  private readonly cartService = inject(CartService)
  private readonly emailService = inject(EmailService)
  isAuthenticated = false;
  username = "";
  userId: string = '';
  userRoles: string[] = [];
  cartToralPrice: number = 0;
  cartTotalQuantity: number = 0;
  hasNewMessage = false;

  ngOnInit(): void {
    this.oidcSecurityService.isAuthenticated$.subscribe(
      ({ isAuthenticated }) => {
        this.isAuthenticated = isAuthenticated;
        if (isAuthenticated) {
          this.loadRoles();
          this.loadUserId();

          if (this.userId) {
            this.emailService.getNewMessageStatus(this.userId).subscribe(hasNewMessage => {
              this.hasNewMessage = hasNewMessage;
            });
          }

          this.emailService.checkForNewMessages(this.userRoles, this.userId);

          if (this.userId) {
            this.cartService.getCartTotalPrice(this.userId).subscribe((totalPrice) => {
              this.cartToralPrice = totalPrice;

              this.cartService.updateCartPrice(totalPrice);
            });
          }

          this.cartService.cartPrice$.subscribe((totalPrice) => {
            this.cartToralPrice = totalPrice;
          });

          if (this.userId) {
            this.cartService.getCartTotalQuantity(this.userId).subscribe((totalQuantity) => {
              this.cartTotalQuantity = totalQuantity;
              console.log('totalQuantity - ', totalQuantity);

              this.cartService.updateCartQuantity(totalQuantity);
            });
          }

          this.cartService.cartQuantity$.subscribe((totalQuantity) => {
            this.cartTotalQuantity = totalQuantity;
          });
        }
      }
    );

    this.oidcSecurityService.userData$.subscribe(
      ({ userData }) => {
        this.username = userData.preferred_username;
      }
    );
  }


  private loadUserId(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.sub) {
        this.userId = decodedToken.sub;

        // Now that we have the userId, check for messages
        this.checkForMessages();
      }
    });
  }

   // Decode JWT token method
   private decodeToken(token: string): any {
    try {
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

  login(): void {
    this.oidcSecurityService.authorize();
  }

  logout(): void {
    this.oidcSecurityService
      .logoff()
      .subscribe((result) => console.log(result));
  }

  // Method to redirect to cart
  redirectToCart(): void {
    if (this.isAuthenticated && this.userId) {
      this.cartService.getCart(this.userId).subscribe(
        (cart) => {
          this.router.navigate(['/cart']);  // Navigate to the cart page after successful fetch
        },
        (error) => {
          console.error('Error fetching cart:', error);
        }
      );
    } else {
      console.error('User is not authenticated or user ID is missing');
    }
  }

  private checkForMessages(): void {
    if (this.userId && this.userRoles.length > 0) {
      this.emailService.getNewMessageStatus(this.userId).subscribe(hasNewMessage => {
        this.hasNewMessage = hasNewMessage;
      });

      this.emailService.checkForNewMessages(this.userRoles, this.userId);
    }
  }

  private loadRoles(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.realm_access) {
        this.userRoles = decodedToken.realm_access.roles || [];

        if (this.userId) {
          this.checkForMessages();
        }
      }
    });
  }

  hasUserRole(): boolean {
    return this.userRoles.includes('ROLE_USER');
  }

  hasAdminRole(): boolean {
    return this.userRoles.includes('ROLE_ADMIN');
  }

}
