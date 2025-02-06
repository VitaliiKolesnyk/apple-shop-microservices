import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import { Message } from '../message';
import { MessageDto } from '../message.dto';

@Injectable({
  providedIn: 'root',
})
export class EmailService {
  private newMessageSubject = new BehaviorSubject<boolean>(false);
  private newMessageMap: Map<string, BehaviorSubject<boolean>> = new Map();

  private apiUrl = 'http://localhost:9000/api/contactMessage';

  constructor(private http: HttpClient) {}

  getNewMessageStatus(userId: string): Observable<boolean> {
    if (!this.newMessageMap.has(userId)) {
      this.newMessageMap.set(userId, new BehaviorSubject<boolean>(false));
    }

    return this.newMessageMap.get(userId)!.asObservable();
  }

  setNewMessageStatus(userId: string, hasNewMessage: boolean): void {
    if (!this.newMessageMap.has(userId)) {
      this.newMessageMap.set(userId, new BehaviorSubject<boolean>(hasNewMessage));
    } else {
      this.newMessageMap.get(userId)!.next(hasNewMessage);
    }
  }

  checkForNewMessages(userRoles: string[], userId: string): void {
    this.getAllEmails().subscribe((messages: Message[]) => {
      const hasNewMessages = messages.some(message =>
        (userRoles.includes('ROLE_ADMIN') && message.lastReplyFrom === 'USER') ||
        (userRoles.includes('ROLE_USER') && message.lastReplyFrom === 'ADMIN')
      );
      this.setNewMessageStatus(userId, hasNewMessages);
    });
  }

  getAllEmails(): Observable<Message[]> {
    return this.http.get<Message[]>(this.apiUrl);
  }

  getAllEmailsByUserId(userId: string): Observable<Message[]> {
    return this.http.get<Message[]>(`${this.apiUrl}?userId=${userId}`);
  }

  getEmailById(id: number): Observable<MessageDto> {
    return this.http.get<MessageDto>(`${this.apiUrl}/${id}`);
  }

  replyToEmail(id: number, message: { emailBody: string, email: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/reply`, message);
  }

  createEmail(message: Message): Observable<any> {
    return this.http.post<any>(this.apiUrl, message);
  }

  updateEmailStatus(id: number, status: string): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, { status });
  }

  deleteEmail(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
}
