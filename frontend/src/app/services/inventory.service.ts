import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InventoryDto } from '../inventory-dto';// Assuming this is the model file

@Injectable({
  providedIn: 'root'
})
export class InventoryService {
  private apiUrl = 'http://localhost:9000/api/inventory'; // Replace with your actual backend URL

  constructor(private http: HttpClient) {}

  getAllInventories(): Observable<InventoryDto[]> {
    return this.http.get<InventoryDto[]>(this.apiUrl);
  }

  getBySkuCode(skuCode: string): Observable<InventoryDto> {
    return this.http.get<InventoryDto>(`${this.apiUrl}/${skuCode}`);
  }

  updateInventory(inventory: InventoryDto): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${inventory.id}`, inventory);
  }

  createInventory(inventory: Partial<InventoryDto>): Observable<InventoryDto> {
    return this.http.post<InventoryDto>(`${this.apiUrl}`, inventory);
  }
}
