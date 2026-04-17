import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

// Install jwt-decode first:
// npm install jwt-decode

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface JwtResponse {
  token: string;
  username: string;
  email: string;
  roles: string[];
}

// Shape of decoded JWT payload
interface JwtPayload {
  sub: string;       // username
  roles: string[];
  iat: number;       // issued at
  exp: number;       // expiry
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  // ── LOGIN ──────────────────────────────────────────────────────────
  login(request: LoginRequest): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap(response => {
          // Store token immediately when login succeeds
          this.storeToken(response.token);
        })
      );
  }

  // ── REGISTER ───────────────────────────────────────────────────────
  register(request: RegisterRequest): Observable<string> {
    // Some backends return a plain text message (201/200) instead of JSON.
    // Request the response as text to avoid JSON parse errors that trigger
    // the error handler and prevent the component from navigating.
    return this.http.post(`${this.apiUrl}/register`, request, { responseType: 'text' });
  }

  // ── STORE TOKEN ────────────────────────────────────────────────────
  storeToken(token: string): void {
    localStorage.setItem('token', token);
  }

  // ── GET TOKEN ──────────────────────────────────────────────────────
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  // ── IS LOGGED IN ───────────────────────────────────────────────────
  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const decoded = jwtDecode<JwtPayload>(token);
      // exp is in seconds, Date.now() is in milliseconds
      return decoded.exp * 1000 > Date.now();
    } catch {
      return false;
    }
  }

  // ── GET USERNAME ───────────────────────────────────────────────────
  getUsername(): string {
    const token = this.getToken();
    if (!token) return '';
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return decoded.sub;
    } catch {
      return '';
    }
  }

  // ── GET ROLES ──────────────────────────────────────────────────────
  getRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return decoded.roles || [];
    } catch {
      return [];
    }
  }

  // ── HAS ROLE ───────────────────────────────────────────────────────
  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  // ── HAS ANY ROLE ───────────────────────────────────────────────────
  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.getRoles().includes(role));
  }

  // ── LOGOUT ─────────────────────────────────────────────────────────
  logout(): void {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}