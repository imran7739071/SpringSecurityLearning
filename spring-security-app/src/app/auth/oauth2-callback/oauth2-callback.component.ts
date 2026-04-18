import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-oauth2-callback',
  template: `
    <div style="display:flex; align-items:center;
                justify-content:center; height:100vh;
                font-family:sans-serif; color:#666;
                flex-direction: column; gap: 1rem;">
      <div>Signing you in...</div>
    </div>
  `
})
export class Oauth2CallbackComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (token) {
        this.authService.storeToken(token);
        this.router.navigate(['/dashboard']);
      } else {
        this.router.navigate(['/login']);
      }
    });
  }
}