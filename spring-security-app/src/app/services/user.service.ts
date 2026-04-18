import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  enabled: boolean;
  oauthProvider: string;
  roles: string[];
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getAllUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.apiUrl}/users`);
  }

  assignRole(userId: number, roleName: string): Observable<UserResponse> {
    return this.http.put<UserResponse>(
      `${this.apiUrl}/users/${userId}/role`,
      { roleName }
    );
  }

  toggleStatus(userId: number): Observable<UserResponse> {
    return this.http.put<UserResponse>(
      `${this.apiUrl}/users/${userId}/toggle-status`,
      {}
    );
  }

  deleteUser(userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/users/${userId}`);
  }
}