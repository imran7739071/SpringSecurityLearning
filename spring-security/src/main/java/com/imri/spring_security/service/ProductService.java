package com.imri.spring_security.service;

import com.imri.spring_security.dto.ProductRequest;
import com.imri.spring_security.dto.ProductResponse;
import com.imri.spring_security.entity.Product;
import com.imri.spring_security.entity.User;
import com.imri.spring_security.exception.ResourceNotFoundException;
import com.imri.spring_security.repository.ProductRepository;
import com.imri.spring_security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ── CREATE ────────────────────────────────────────────────────────
    public ProductResponse create(ProductRequest request) {

        // Get the currently logged-in username from SecurityContext
        // This is available because JwtAuthFilter set the authentication
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + username));

        // Map DTO → Entity
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(request.getCategory());
        product.setCreatedBy(currentUser);   // link to logged-in user

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    // ── READ ALL ──────────────────────────────────────────────────────
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── READ ONE ──────────────────────────────────────────────────────
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));
        return mapToResponse(product);
    }

    // ── READ BY CATEGORY ──────────────────────────────────────────────
    public List<ProductResponse> getByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── SEARCH ────────────────────────────────────────────────────────
    public List<ProductResponse> search(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── UPDATE ────────────────────────────────────────────────────────
    public ProductResponse update(Long id, ProductRequest request) {
        // Find existing product — throws 404 if not found
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found with id: " + id));

        // Update only the fields that were sent
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(request.getCategory());
        // Note: createdBy is NOT updated — ownership doesn't change

        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────────
    public void delete(Long id) {
        // Verify it exists before deleting
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // ── MAPPER: Entity → Response DTO ─────────────────────────────────
    // Private helper — keeps mapping logic in one place
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(product.getCategory())
                .createdBy(product.getCreatedBy() != null
                        ? product.getCreatedBy().getUsername()
                        : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}