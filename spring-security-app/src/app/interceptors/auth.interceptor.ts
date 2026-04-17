import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router) {}

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {

    // Get token from localStorage
    const token = localStorage.getItem('token');

    // If token exists, clone the request and add Authorization header
    // We clone because HttpRequest is immutable
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    // Pass to next handler, catch errors
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {

        if (error.status === 401) {
          // Token expired or invalid — clear storage and go to login
          localStorage.removeItem('token');
          this.router.navigate(['/login']);
        }

        if (error.status === 403) {
          // Logged in but wrong role
          this.router.navigate(['/forbidden']);
        }

        return throwError(() => error);
      })
    );
  }
}