import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService } from '../../services/product.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {

  productForm: FormGroup;
  isEditMode = false;
  productId: number | null = null;

  isLoading = false;
  isSaving = false;
  errorMessage = '';

  categories = [
    'Electronics',
    'Clothing',
    'Food',
    'Books',
    'Furniture',
    'Sports',
    'Other'
  ];

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.productForm = this.fb.group({
      name:        ['', [Validators.required, Validators.minLength(2)]],
      description: ['', [Validators.maxLength(500)]],
      price:       [null, [Validators.required, Validators.min(0.01)]],
      quantity:    [null, [Validators.required, Validators.min(0)]],
      category:    ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Check if 'id' exists in URL — if yes, it's edit mode
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.isEditMode = true;
      this.productId = +id;   // '+' converts string to number
      this.loadProduct(this.productId);
    }
  }

  // Load existing product data into the form for editing
  loadProduct(id: number): void {
    this.isLoading = true;

    this.productService.getById(id).subscribe({
      next: (product) => {
        // Patch fills only the matching fields
        this.productForm.patchValue({
          name:        product.name,
          description: product.description,
          price:       product.price,
          quantity:    product.quantity,
          category:    product.category
        });
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'Product not found.';
        this.isLoading = false;
      }
    });
  }

  get f() { return this.productForm.controls; }

  onSubmit(): void {
    if (this.productForm.invalid) return;

    this.isSaving = true;
    this.errorMessage = '';

    const formValue = this.productForm.value;

    if (this.isEditMode && this.productId) {
      // UPDATE
      this.productService.update(this.productId, formValue).subscribe({
        next: () => {
          this.isSaving = false;
          this.router.navigate(['/products']);
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = err.error?.message || 'Update failed.';
        }
      });
    } else {
      // CREATE
      this.productService.create(formValue).subscribe({
        next: () => {
          this.isSaving = false;
          this.router.navigate(['/products']);
        },
        error: (err) => {
          this.isSaving = false;
          this.errorMessage = err.error?.message || 'Create failed.';
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/products']);
  }
}