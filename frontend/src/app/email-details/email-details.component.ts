import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EmailService } from '../services/email.service';
import { MessageDto } from '../message.dto';
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-email-details',
  templateUrl: './email-details.component.html',
  styleUrls: ['./email-details.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class EmailDetailsComponent implements OnInit {
  message: MessageDto | undefined;
  replyText: string = '';
  userEmail: string = ''
  userId: string = ''
  closeSuccessMessage: boolean = false;
  userRoles: string[] = [];
  hasReplied: boolean = false;

  constructor(private emailService: EmailService, private route: ActivatedRoute,
              private oidcSecurityService: OidcSecurityService) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loadRoles()
    this.loadUserEmail()
    this.loadMessage(id);
    this.loadUserId();
  }

  loadMessage(id: number) {
    this.emailService.getEmailById(id).subscribe((data: MessageDto) => {
      this.message = data;
    });
  }

  private loadUserId(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.sub) {
        this.userId = decodedToken.sub;
      }
    });
  }

  updateStatus(messageId: number, newStatus: string) {
    if (this.message && this.message.id) {
      this.emailService.updateEmailStatus(messageId, newStatus).subscribe(() => {
        this.loadMessage(messageId);
        this.showCloseSuccessMessage();
      });
    } else {
      console.error('Message is undefined or null');
    }
  }

  sendReply() {
    if (this.message && this.replyText.trim()) {
      const replyData = { emailBody: this.replyText, email: this.userEmail, isAdmin: this.hasAdminRole() };

      this.emailService.replyToEmail(this.message.id, replyData).subscribe(() => {
        this.loadMessage(this.message!.id);
        this.replyText = '';

        this.hasReplied = true;

        if (this.hasUserRole()) {
          this.emailService.setNewMessageStatus(this.userId, false);
        }
      });
    }
  }

  private loadUserEmail(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.email) {
        this.userEmail = decodedToken.email;
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

  showCloseSuccessMessage() {
    this.closeSuccessMessage = true;
    setTimeout(() => {
      this.closeSuccessMessage = false;
    }, 3000);  // Hide message after 3 seconds
  }

  private loadRoles(): void {
    this.oidcSecurityService.getAccessToken().subscribe((token) => {
      const decodedToken = this.decodeToken(token);
      if (decodedToken && decodedToken.realm_access) {
        this.userRoles = decodedToken.realm_access.roles || [];
      }
    });
  }

  hasAdminRole(): boolean {
    return this.userRoles.includes('ROLE_ADMIN');
  }

  hasUserRole(): boolean {
    return this.userRoles.includes('ROLE_USER');
  }
}
