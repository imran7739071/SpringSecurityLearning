import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';

// Auth components
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { Oauth2CallbackComponent } from './auth/oauth2-callback/oauth2-callback.component';

// Dashboard
import { DashboardComponent } from './dashboard/dashboard/dashboard.component';

// Admin
import { AdminComponent } from './admin/admin/admin.component';

// Shared
import { NavbarComponent } from './shared/navbar/navbar.component';
import { ForbiddenComponent } from './shared/forbidden/forbidden.component';

// Interceptor
import { AuthInterceptor } from './interceptors/auth.interceptor';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    Oauth2CallbackComponent,
    DashboardComponent,
    AdminComponent,
    NavbarComponent,
    ForbiddenComponent,
  ],
  imports: [
    BrowserModule,
    HttpClientModule,         // enables HttpClient in all services
    ReactiveFormsModule,      // for login/register forms
    FormsModule,              // for ngModel
    AppRoutingModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true,            // allow multiple interceptors
    },
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}