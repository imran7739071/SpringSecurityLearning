import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ProductRequest {
  name: string;
  description: string;
  price: number;
  quantity: number;
  category: string;
}

export interface ProductResponse {
  id: number;
  name: string;
  description: string;
  price: number;
  quantity: number;
  category: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  private apiUrl = 'http://localhost:8080/api/products';

  constructor(private http: HttpClient) {}

  // GET all products
  getAll(): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>(this.apiUrl);
  }

  // GET by ID
  getById(id: number): Observable<ProductResponse> {
    return this.http.get<ProductResponse>(`${this.apiUrl}/${id}`);
  }

  // GET by category
  getByCategory(category: string): Observable<ProductResponse[]> {
    return this.http.get<ProductResponse[]>(
      `${this.apiUrl}/category/${category}`
    );
  }

  // GET search
  search(keyword: string): Observable<ProductResponse[]> {
    const params = new HttpParams().set('keyword', keyword);
    return this.http.get<ProductResponse[]>(
      `${this.apiUrl}/search`, { params }
    );
  }

  // POST create
  create(product: ProductRequest): Observable<ProductResponse> {
    return this.http.post<ProductResponse>(this.apiUrl, product);
  }

  // PUT update
  update(id: number, product: ProductRequest): Observable<ProductResponse> {
    return this.http.put<ProductResponse>(`${this.apiUrl}/${id}`, product);
  }

  // DELETE
  delete(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}