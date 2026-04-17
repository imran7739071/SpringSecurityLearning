import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  Router
} from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {

    // Read required roles from route data
    // e.g. data: { roles: ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN'] }
    const requiredRoles = route.data['roles'] as string[];

    if (!requiredRoles || requiredRoles.length === 0) {
      return true;   // no roles defined = allow everyone
    }

    if (this.authService.hasAnyRole(requiredRoles)) {
      return true;   // user has at least one required role
    }

    // Logged in but wrong role
    this.router.navigate(['/forbidden']);
    return false;
  }
}