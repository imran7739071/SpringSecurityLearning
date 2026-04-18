import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  username = '';
  roles: string[] = [];

  constructor(public authService: AuthService) {}

  ngOnInit(): void {
    this.username = this.authService.getUsername();
    this.roles = this.authService.getRoles();
  }
}