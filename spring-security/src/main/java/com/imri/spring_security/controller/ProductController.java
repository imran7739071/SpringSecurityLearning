package com.imri.spring_security.controller;

import com.imri.spring_security.dto.ProductRequest;
import com.imri.spring_security.dto.ProductResponse;
import com.imri.spring_security.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ProductController {

    private final ProductService productService;

    // GET /api/products — all roles can read
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    // GET /api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // GET /api/products/category/Electronics
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponse>> getByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(productService.getByCategory(category));
    }

    // GET /api/products/search?keyword=phone
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponse>> search(
            @RequestParam String keyword) {
        return ResponseEntity.ok(productService.search(keyword));
    }

    // POST /api/products — ADMIN and MANAGER only
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ProductResponse> create(
            @RequestBody @Valid ProductRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    // PUT /api/products/1 — ADMIN and MANAGER only
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER')")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    // DELETE /api/products/1 — SUPER_ADMIN and ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, String>> delete(
            @PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(
                Map.of("message", "Product deleted successfully"));
    }
}