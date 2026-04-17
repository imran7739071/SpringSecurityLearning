import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Oauth2CallbackComponent } from './auth/oauth2-callback/oauth2-callback.component';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { DashboardComponent } from './dashboard/dashboard/dashboard.component';
import { AuthGuard } from './guards/auth.guard';
import { ForbiddenComponent } from './shared/forbidden/forbidden.component';
import { AdminComponent } from './admin/admin/admin.component';
import { RoleGuard } from './guards/role.guard';

const routes: Routes = [

  // Public routes — no guard needed
  { path: 'login',           component: LoginComponent },
  { path: 'register',        component: RegisterComponent },
  { path: 'oauth2/callback', component: Oauth2CallbackComponent },
  { path: 'forbidden',       component: ForbiddenComponent },

  // Protected — must be logged in
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard]
  },

  // Protected — must be ADMIN or SUPER_ADMIN
  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: { roles: ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN'] }
  },

  // Default redirect
  { path: '',   redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' },    // wildcard — unknown URLs
];


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
