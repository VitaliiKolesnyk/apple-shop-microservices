import { Component, OnInit } from '@angular/core';
import { EmailService } from '../services/email.service';
import { Message } from '../message';
import { CommonModule } from "@angular/common";
import { Router } from "@angular/router";
import { OidcSecurityService } from 'angular-auth-oidc-client';
import { NgxPaginationModule } from 'ngx-pagination';
import { FormsModule } from "@angular/forms";

@Component({
  selector: 'app-admin-contact-board',
  templateUrl: './admin-contact-board.component.html',
  styleUrls: ['./admin-contact-board.component.css'],
  standalone: true,
  imports: [CommonModule, NgxPaginationModule, FormsModule]
})
export class AdminContactBoardComponent implements OnInit {
  contactMessages: Message[] = [];
  filteredMessages: Message[] = []; // Messages to show based on pagination
  isAuthenticated = false;
  userRoles: string[] = [];
  userId = '';
  selectedStatus: string = 'OPEN'; // Default to showing OPEN emails

  // Pagination properties
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalItems: number = 0;
  totalPages: number = 0;
  itemsPerPageOptions: number[] = [5, 10, 15, 20];

  constructor(private router: Router, private emailService: EmailService,
              private oidcSecurityService: OidcSecurityService) {}

  ngOnInit() {
    this.oidcSecurityService.isAuthenticated$.subscribe(({ isAuthenticated }) => {
      this.isAuthenticated = isAuthenticated;
      if (isAuthenticated) {
        this.loadRoles();
        this.loadUserId();
        this.loadMessages();
      }
    });
  }

  loadMessages() {
    if (this.hasAdminRole()) {
      this.emailService.getAllEmails().subscribe((data: Message[]) => {
        this.filterMessages(data);
      });
    } else {
      this.emailService.getAllEmailsByUserId(this.userId).subscribe((data: Message[]) => {
        this.filterMessages(data);
      });
    }
  }

  // Handle the change in the dropdown filter
  onStatusChange(event: any) {
    this.selectedStatus = event.target.value;
    this.loadMessages(); // Reload messages based on the new status
  }

  // Method to update the status of an email
  updateStatus(messageId: number, newStatus: string) {
    this.emailService.updateEmailStatus(messageId, newStatus).subscribe(() => {
      this.loadMessages(); // Reload messages after status update
    });
  }

  // Method to delete an email
  deleteEmail(messageId: number) {
    if (confirm('Are you sure you want to delete this email?')) {
      this.emailService.deleteEmail(messageId).subscribe(() => {
        this.loadMessages(); // Reload messages after deletion
      });
    }
  }

  // Pagination logic to display current page's messages
  paginateMessages() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.filteredMessages = this.contactMessages.slice(start, end);
  }

  // Handle page change
  onPageChange(page: number) {
    this.currentPage = page;
    this.paginateMessages();
  }

  onItemsPerPageChange(event: any) {
    this.itemsPerPage = +event.target.value;
    this.currentPage = 1; // Reset to first page
    this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
    this.paginateMessages();
  }

  // Navigation controls
  goToFirstPage() {
    this.currentPage = 1;
    this.paginateMessages();
  }

  goToLastPage() {
    this.currentPage = this.totalPages;
    this.paginateMessages();
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.paginateMessages();
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.paginateMessages();
    }
  }

  navigateToMessage(messageId: number) {
    this.router.navigate([`/email/${messageId}`]);
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
      }
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

  createNewRequest() {
    this.router.navigate(['/contact-us']);
  }

  hasAdminRole(): boolean {
    return this.userRoles.includes('ROLE_ADMIN');
  }

  hasUserRole(): boolean {
    return this.userRoles.includes('ROLE_USER');
  }

  getMessageClass(message: Message): string {
    if (message.status === 'OPEN') {
      if (this.hasAdminRole() && message.lastReplyFrom === 'USER') {
        return 'highlight-admin'; // Admin should respond
      } else if (this.hasUserRole() && message.lastReplyFrom === 'ADMIN') {
        return 'highlight-user'; // User should respond
      }
    }
    return ''; // No highlight if conditions are not met
  }

  shouldShowLastReplyAt(message: Message): boolean {
    return this.getMessageClass(message) !== ''; // Show if the message is highlighted
  }

  isNewMessage(message: Message): boolean {
    if (this.hasAdminRole() && message.lastReplyFrom === 'USER') {
      return true; // Admin sees "New Message" for USER replies
    } else if (this.hasUserRole() && message.lastReplyFrom === 'ADMIN') {
      return true; // User sees "New Message" for ADMIN replies
    }
    return false;
  }

  // Filter messages based on the selected status and prioritize highlighted ones
  filterMessages(messages: Message[]) {
    let filteredMessages = messages;

    if (this.selectedStatus === 'OPEN') {
      filteredMessages = messages.filter((message) => message.status === 'OPEN');
    } else if (this.selectedStatus === 'CLOSED') {
      filteredMessages = messages.filter((message) => message.status === 'CLOSED');
    }

    this.contactMessages = filteredMessages.sort((a, b) => {
      const isAHighlighted = this.isMessageHighlighted(a);
      const isBHighlighted = this.isMessageHighlighted(b);

      if (isAHighlighted && !isBHighlighted) return -1; // Highlighted messages come first
      if (!isAHighlighted && isBHighlighted) return 1;  // Non-highlighted messages come later

      // If both are highlighted or both are not, sort by lastReplyAt (most recent first)
      return new Date(b.lastReplyAt).getTime() - new Date(a.lastReplyAt).getTime();
    });

    this.totalItems = this.contactMessages.length;
    this.paginateMessages();

    const highlightedMessageExists = this.contactMessages.some((message) => this.isMessageHighlighted(message));
    this.emailService.setNewMessageStatus(this.userId, highlightedMessageExists);
  }

// Check if the message should be highlighted
  isMessageHighlighted(message: Message): boolean {
    return (this.hasAdminRole() && message.lastReplyFrom === 'USER') ||
      (this.hasUserRole() && message.lastReplyFrom === 'ADMIN');
  }

}
