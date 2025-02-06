import { Component, OnInit } from '@angular/core';
import { loadStripe, Stripe } from '@stripe/stripe-js';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

interface PaymentState {
  amount: number;
  userId: string;
  userEmail: string;
  orderNumber: string;
  skuCodes: string [];
}

@Component({
  selector: 'app-payment',
  template: '<ng-container></ng-container>',  // No HTML needed
  styleUrls: ['./payment.component.css'],
  standalone: true,
  imports: [CommonModule]
})
export class PaymentComponent implements OnInit {
  stripe: Stripe | null = null;
  amount: number;
  userId: string;
  userEmail: string;
  orderNumber: string;
  skuCodes: string [];
  paymentMessage: string = ''; // Message for the popup
  showPopup = false; // Control popup visibility
  isPaymentProcessing = false;

  constructor(private http: HttpClient, private router: Router) {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras.state as PaymentState;

    this.amount = state?.amount || 0;
    this.userId = state?.userId || '';
    this.userEmail = state?.userEmail || '';
    this.orderNumber = state?.orderNumber || '';
    this.skuCodes = state?.skuCodes || []
    console.log("Order number in payment component: " + this.orderNumber);
  }

  async ngOnInit() {
    try {
      // Initialize Stripe
      this.stripe = await loadStripe('pk_test_51Q9R43LI63ZkzNayLCS4C7vvSJL9R31ans6GcN4m528CXXUTkLaBHY4SIKzTXlzDDC85DGH6nZQWWIJnRkMeZGtI00z5L5xYzI'); // Replace with your real public key

      if (this.stripe) {
        await this.handleSubmit();
      } else {
        throw new Error('Stripe.js failed to initialize.');
      }
    } catch (error) {
      console.error('Error initializing Stripe:', error);
      this.paymentMessage = 'Failed to load payment methods.';
      this.showPopup = true;
    }
  }

  async handleSubmit() {
    this.isPaymentProcessing = true;

    if (!this.stripe) {
      console.error('Stripe.js has not been initialized.');
      this.isPaymentProcessing = false;
      return;
    }

    const checkoutData = {
      amount: this.amount,
      userId: this.userId,
      userEmail: this.userEmail,
      orderNumber: this.orderNumber,
      skuCodes: this.skuCodes
    };

    this.http.post<{ sessionId: string }>('http://localhost:9000/api/payments/create-checkout-session', checkoutData).subscribe(
      async (response) => {
        console.log('Stripe session created:', response);  // Log the response
        const sessionId = response.sessionId;
        console.log('SessionId:', sessionId);

        if (!sessionId) {
          console.error('No sessionId received from the backend.');
          this.isPaymentProcessing = false;
          this.paymentMessage = 'Failed to create a session. Please try again.';
          this.showPopup = true;
          return;
        }

        // Redirect to Stripe Checkout using the sessionId
        const result = await this.stripe?.redirectToCheckout({ sessionId });

        if (result?.error) {
          console.error('Error redirecting to Stripe Checkout:', result.error.message);
          this.isPaymentProcessing = false;
          this.paymentMessage = 'Error redirecting to payment page. Please try again.';
          this.showPopup = true;
        }
      },
      (error) => {
        console.error('Error creating Stripe session:', error);
        this.isPaymentProcessing = false;
        this.paymentMessage = 'Failed to create a session. Please try again.';
        this.showPopup = true;
      }
    );
  }
}
