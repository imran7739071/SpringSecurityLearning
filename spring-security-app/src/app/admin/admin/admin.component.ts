import { Component, OnInit } from '@angular/core';
import { UserService, UserResponse } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {

  users: UserResponse[] = [];
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  // For the role change dropdown
  availableRoles = [
    'ROLE_USER',
    'ROLE_MANAGER',
    'ROLE_ADMIN',
    'ROLE_SUPER_ADMIN'
  ];

  // Track which user's role dropdown is open
  selectedRoles: { [userId: number]: string } = {};

  constructor(
    public authService: AuthService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        // Pre-select current role in dropdown for each user
        data.forEach(u => {
          this.selectedRoles[u.id] = u.roles[0] || 'ROLE_USER';
        });
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Failed to load users.';
        this.isLoading = false;
      }
    });
  }

  assignRole(userId: number): void {
    const roleName = this.selectedRoles[userId];
    console.log(roleName);
    this.userService.assignRole(userId, roleName).subscribe({
      next: (updated) => {
        // Update the user in the list without reloading
        const index = this.users.findIndex(u => u.id === userId);
        if (index !== -1) this.users[index] = updated;
        this.showSuccess('Role updated successfully');
      },
      error: () => {
        this.errorMessage = 'Failed to update role.';
      }
    });
  }

  toggleStatus(user: UserResponse): void {
    this.userService.toggleStatus(user.id).subscribe({
      next: (updated) => {
        const index = this.users.findIndex(u => u.id === user.id);
        if (index !== -1) this.users[index] = updated;
        this.showSuccess(
          `User "${user.username}" ${updated.enabled ? 'enabled' : 'disabled'}`
        );
      },
      error: () => {
        this.errorMessage = 'Failed to update status.';
      }
    });
  }

  deleteUser(user: UserResponse): void {
    if (!confirm(`Delete user "${user.username}"? This cannot be undone.`)) return;

    this.userService.deleteUser(user.id).subscribe({
      next: () => {
        this.users = this.users.filter(u => u.id !== user.id);
        this.showSuccess(`User "${user.username}" deleted.`);
      },
      error: () => {
        this.errorMessage = 'Failed to delete user.';
      }
    });
  }

  isSuperAdmin(): boolean {
    return this.authService.hasRole('ROLE_SUPER_ADMIN');
  }

  // Auto-clear success message after 3 seconds
  showSuccess(message: string): void {
    this.successMessage = message;
    this.errorMessage = '';
    setTimeout(() => this.successMessage = '', 3000);
  }

  getRoleBadgeClass(role: string): string {
    const map: { [key: string]: string } = {
      'ROLE_SUPER_ADMIN': 'badge-super',
      'ROLE_ADMIN':       'badge-admin',
      'ROLE_MANAGER':     'badge-manager',
      'ROLE_USER':        'badge-user'
    };
    return map[role] || 'badge-user';
  }

  get activeUserCount(): number {
    return this.users.filter(u => u.enabled).length;
  }
}