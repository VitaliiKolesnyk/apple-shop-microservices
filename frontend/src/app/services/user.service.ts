import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { User } from '../user';
import { UpdatePasswordDto } from '../update.pasword.dto';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly API_URL = 'http://localhost:9000/api/users';

  constructor(private httpClient: HttpClient) {}

  getUser(): Observable<User> {
    return this.httpClient.get<User>(`${this.API_URL}`);
  }

  updateUser(user: User): Observable<any> {
    return this.httpClient.post(this.API_URL, user);
  }

  updatePassword(userId: string, updatePasswordDto: UpdatePasswordDto): Observable<any> {
    return this.httpClient.put(`${this.API_URL}/updatePassword/${userId}`, updatePasswordDto);
  }

}
