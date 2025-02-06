import {Component, OnInit} from '@angular/core';
import {Router, RouterModule} from '@angular/router';
import {OidcSecurityService} from "angular-auth-oidc-client";
import {CommonModule} from "@angular/common";
import {EmailService} from "../services/email.service";

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './user-dashboard.component.html',
  styleUrl: './user-dashboard.component.css'
})
export class UserDashboardComponent implements OnInit{
  userRoles: string[] = [];
  isAuthenticated = false;
  userId: string = '';
  hasNewMessage = false;

  constructor(private oidcSecurityService: OidcSecurityService,
              private emailService: EmailService) {}

  ngOnInit() {
    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;
      if (isAuthenticated) {
        this.loadRoles();
        this.loadUserId();

        // Subscribe to new message status for the current user
        if (this.userId) {
          this.emailService
            .getNewMessageStatus(this.userId)
            .subscribe((hasNewMessage) => {
              this.hasNewMessage = hasNewMessage;
            });
        }

        this.emailService.checkForNewMessages(this.userRoles, this.userId);
      }
    });
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

  private loadRoles(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.realm_access) {
        this.userRoles = decodedToken.realm_access.roles || [];
        this.emailService.checkForNewMessages(this.userRoles, decodedToken.sub);
      }
    });
  }

  hasUserRole(): boolean {
    return this.userRoles.includes('ROLE_USER');
  }

  private loadUserId(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.sub) {
        this.userId = decodedToken.sub;
        this.checkForMessages()
      }
    });
  }

  private checkForMessages(): void {
    if (this.userId && this.userRoles.length > 0) {
      this.emailService.checkForNewMessages(this.userRoles, this.userId);
    }
  }
}
