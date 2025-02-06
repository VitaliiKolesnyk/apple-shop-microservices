import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UserService } from '../services/user.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { UserDashboardComponent } from '../user-dashboard/user-dashboard.component';
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { User } from '../user';

@Component({
  selector: 'app-user-info',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule, UserDashboardComponent],
  templateUrl: './user-info.component.html',
  styleUrls: ['./user-info.component.css']
})
export class UserInfoComponent implements OnInit {
  user: User | null = null;
  message: string | null = null;
  isSuccess: boolean = false;
  password: string = '';
  confirmPassword: string = '';
  showPasswordForm = false;
  isPasswordValid: boolean = false;

  passwordConditions = {
    length: false,
    uppercase: false,
    lowercase: false,
    number: false,
    special: false
  };

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private oidcSecurityService: OidcSecurityService
  ) {}

  ngOnInit() {
      this.fetchUserDetails();
  }

  fetchUserDetails() {
    this.userService.getUser().subscribe(
      (user) => {
        this.user = user;
        console.log('Fetched user info:', this.user);
      },
      (error) => {
        console.error('Error fetching user info:', error);
      }
    );
  }

  updateUser() {
    if (this.user) {
      this.userService.updateUser(this.user).subscribe(
        (response) => {
          this.showMessage('Personal information updated successfully', true);
          console.log('User updated successfully', response);
          this.isSuccess = true;
        },
        (error) => {
          this.showMessage('Error updating user.', false);
          console.error('Error updating user', error);
        }
      );
    }
  }

  private showMessage(msg: string, isSuccess: boolean) {
    this.message = msg;
    this.isSuccess = isSuccess;
    setTimeout(() => {
      this.message = null;
    }, 10000);
  }

  updatePassword() {
    if (this.password !== this.confirmPassword) {
      this.showMessage('Passwords do not match', false);
      return;
    }

    const updatePasswordDto = {
      value: this.password
    };

    this.oidcSecurityService.getAccessToken().subscribe(
      (token) => {
        const userId = this.decodeToken(token);
        if (userId) {
          this.userService.updatePassword(userId, updatePasswordDto).subscribe(
            (response) => {
              this.showMessage('Password updated successfully', true);
              console.log('Password updated successfully', response);
              this.isSuccess = true;
            },
            (error) => {
              this.showMessage('Your password does not meet the following requirements:\n' +
                '\n' +
                '- At least 8 characters in length.\n' +
                '- Includes at least one uppercase letter.\n' +
                '- Includes at least one lowercase letter.\n' +
                '- Contains at least one number.\n' +
                '- Includes at least one special character (e.g., !, @, #, $).', false);
              console.error('Error updating password', error);
            }
          );
        }
      },
      (error) => {
        console.error('Failed to get access token:', error);
      }
    );
  }

  // Decode JWT token method
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

  togglePasswordForm() {
    this.showPasswordForm = !this.showPasswordForm;
  }

  validatePassword() {
    const specialCharRegex = /[!@#$%^&*(),.?":{}|<>]/;

    this.passwordConditions.length = this.password.length >= 8;
    this.passwordConditions.uppercase = /[A-Z]/.test(this.password);
    this.passwordConditions.lowercase = /[a-z]/.test(this.password);
    this.passwordConditions.number = /\d/.test(this.password);
    this.passwordConditions.special = specialCharRegex.test(this.password);

    const passwordsMatch = this.password === this.confirmPassword;

    this.isPasswordValid = Object.values(this.passwordConditions).every(Boolean) && passwordsMatch;
  }
}
