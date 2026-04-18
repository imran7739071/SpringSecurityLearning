import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProductService, ProductResponse } from '../../services/product.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss']
})
export class ProductListComponent implements OnInit {

  products: ProductResponse[] = [];
  filteredProducts: ProductResponse[] = [];

  searchKeyword = '';
  selectedCategory = '';
  categories: string[] = [];

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    public authService: AuthService,
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.productService.getAll().subscribe({
      next: (data) => {
        this.products = data;
        this.filteredProducts = data;

        // Extract unique categories for the filter dropdown
        this.categories = [...new Set(data.map(p => p.category))];
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load products.';
        this.isLoading = false;
      }
    });
  }

  // Filter by search keyword — runs on every keyup
  onSearch(): void {
    this.applyFilters();
  }

  // Filter by category — runs on dropdown change
  onCategoryChange(): void {
    this.applyFilters();
  }

  // Combines both filters
  applyFilters(): void {
    this.filteredProducts = this.products.filter(p => {
      const matchesKeyword = p.name
        .toLowerCase()
        .includes(this.searchKeyword.toLowerCase());

      const matchesCategory = this.selectedCategory
        ? p.category === this.selectedCategory
        : true;

      return matchesKeyword && matchesCategory;
    });
  }

  clearFilters(): void {
    this.searchKeyword = '';
    this.selectedCategory = '';
    this.filteredProducts = this.products;
  }

  goToCreate(): void {
    this.router.navigate(['/products/create']);
  }

  goToEdit(id: number): void {
    this.router.navigate(['/products/edit', id]);
  }

  delete(id: number, name: string): void {
    if (!confirm(`Are you sure you want to delete "${name}"?`)) return;

    this.productService.delete(id).subscribe({
      next: () => {
        this.successMessage = `"${name}" deleted successfully.`;
        this.loadProducts();   // reload the list
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: () => {
        this.errorMessage = 'Failed to delete product.';
      }
    });
  }

  // Check if user can create/edit
  canEdit(): boolean {
    return this.authService.hasAnyRole([
      'ROLE_MANAGER', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN'
    ]);
  }

  // Check if user can delete
  canDelete(): boolean {
    return this.authService.hasAnyRole([
      'ROLE_ADMIN', 'ROLE_SUPER_ADMIN'
    ]);
  }
}