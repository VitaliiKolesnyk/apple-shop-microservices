import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CommentDto} from "../comment-dto";
import {CommentRequest} from "../comment.request";

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = 'http://localhost:9000/api/products';

  constructor(private http: HttpClient) {}

  // Get all comments for a product
  getComments(productId: string): Observable<CommentDto[]> {
    return this.http.get<CommentDto[]>(`${this.apiUrl}/${productId}/comments`);
  }

  // Add a new comment to a product
  addComment(productId: string, commentRequest: CommentRequest): Observable<CommentRequest> {
    return this.http.post<CommentDto>(`${this.apiUrl}/${productId}/comments`, commentRequest);
  }

  // Add a reply to a comment
  addReply(productId: string, commentId: string, replyDto: CommentRequest): Observable<CommentRequest> {
    return this.http.post<CommentDto>(`${this.apiUrl}/${productId}/comments/${commentId}`, replyDto);
  }

  // Delete a comment
  deleteComment(productId: string, commentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${productId}/comments/${commentId}`);
  }
}
