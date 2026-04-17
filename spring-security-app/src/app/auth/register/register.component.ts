import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {

  registerForm: FormGroup;
  errorMessage = '';
  successMessage = '';
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email:    ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6),
                      Validators.pattern('^(?=.*[A-Z])(?=.*[0-9]).{6,}$')]],
    });
  }

  get f() { return this.registerForm.controls; }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.register(this.registerForm.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Registered successfully! Redirecting to login...';
        console.log(this.successMessage);
        
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.isLoading = false;

        // Spring Boot can return errors in different shapes:
        if (typeof err.error === 'string') {
          // Plain string error
          this.errorMessage = err.error;
        } else if (err.error?.message) {
          // { message: "..." } shape
          this.errorMessage = err.error.message;
        } else if (err.error?.errors) {
          // Validation errors shape from GlobalExceptionHandler
          // { errors: { username: "required", email: "invalid" } }
          this.errorMessage = Object.values(err.error.errors).join(', ');
        } else {
          this.errorMessage = 'Registration failed. Please try again.';
        }
      }
    });
  }
}