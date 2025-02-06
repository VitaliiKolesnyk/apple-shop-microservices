import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  private apiUrl = 'http://localhost:9000/api/payments';

  constructor(private http: HttpClient) {}

  postPaymentStatus(email: string, status: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}?status=${status}&email=${email}`, {});
  }
}
