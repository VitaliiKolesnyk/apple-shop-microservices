import {Component, OnInit} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { EmailService} from "../services/email.service";
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-contact-us',
  templateUrl: './contact-us.component.html',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  styleUrls: ['./contact-us.component.css']
})
export class ContactUsComponent implements OnInit {
  contactForm: FormGroup;
  userId: string = '';
  isAuthenticated = false;
  userEmail: string = ''
  successMessage: string = '';

  constructor(private fb: FormBuilder, private http: HttpClient, private emailService: EmailService,
              private oidcSecurityService: OidcSecurityService) {
    this.contactForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      subject: ['', [Validators.required]],
      emailBody: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;
      if (isAuthenticated) {
        this.loadUserEmail();
        this.loadUserId();
      }
    });
  }

  private loadUserEmail(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.email) {
        this.userEmail = decodedToken.email;
        this.contactForm.patchValue({ email: this.userEmail });
        this.contactForm.get('email')?.disable(); // Disable the email input
      }
    });
  }

  onSubmit() {
    if (this.contactForm.valid) {
      const contactData = this.contactForm.getRawValue();
      contactData.userId = this.userId;

      this.emailService.createEmail(contactData).subscribe(() => {
        this.successMessage = 'Contact Us request has been sent successfully!';

        // Clear the success message after 3 seconds
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);
      });
    }
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
}
