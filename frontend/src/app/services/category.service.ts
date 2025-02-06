import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category } from '../category';
import { CategoryRequest } from '../category.request';
import {Product} from "../product";

@Injectable({
  providedIn: 'root'
})
export class CategoryService {

  private apiUrl = 'http://localhost:9000/api/products/categories';

  constructor(private http: HttpClient) { }

  addCategory(categoryRequest: FormData): Observable<CategoryRequest> {
    return this.http.post<CategoryRequest>(this.apiUrl, categoryRequest);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.apiUrl);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getCategoryById(categoryId: string): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/` + categoryId)
  }

  updateCategory(categoryId: string, formData: FormData): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${categoryId}`, formData);
  }
}
