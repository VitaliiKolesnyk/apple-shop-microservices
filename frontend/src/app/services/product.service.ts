import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { Product } from '../product';
import { catchError, map } from 'rxjs/operators';
import { ProductPage } from '../product';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private readonly API_URL = 'http://localhost:9000/api/products';

  constructor(private httpClient: HttpClient) {}

  getProducts(page: number = 0, size: number = 5, sort: string = 'name', sortDir: string = 'ASC', categoryId: string = "", search: string = ""): Observable<ProductPage> {
    return this.httpClient.get<ProductPage>(`${this.API_URL}/paged?page=${page}&size=${size}&sortBy=${sort}&sortDir=${sortDir}&&categoryId=${categoryId}&search=${search}`);
  }

  createProduct(formData: FormData): Observable<Product> {
    return this.httpClient.post<Product>(this.API_URL, formData)
  }

  removeProduct(productId: string): Observable<void> {
    return this.httpClient.delete<void>(`${this.API_URL}/` + productId)
  }

  getProductById(productId: string): Observable<Product> {
    return this.httpClient.get<Product>(`${this.API_URL}/` + productId)
  }

  updateProduct(productId: string, formData: FormData): Observable<Product> {
    return this.httpClient.put<Product>(`${this.API_URL}/${productId}`, formData);
  }
}
