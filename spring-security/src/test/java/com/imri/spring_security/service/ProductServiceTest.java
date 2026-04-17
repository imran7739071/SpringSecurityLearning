package com.imri.spring_security.service;

import com.imri.spring_security.dto.ProductRequest;
import com.imri.spring_security.dto.ProductResponse;
import com.imri.spring_security.entity.Product;
import com.imri.spring_security.entity.User;
import com.imri.spring_security.exception.ResourceNotFoundException;
import com.imri.spring_security.repository.ProductRepository;
import com.imri.spring_security.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProductService productService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_shouldPersistProductForAuthenticatedUser() {
        ProductRequest request = buildRequest();
        User currentUser = new User();
        currentUser.setId(7L);
        currentUser.setUsername("imran");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("imran", "password")
        );

        when(userRepository.findByUsername("imran")).thenReturn(Optional.of(currentUser));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(11L);
            product.setCreatedAt(LocalDateTime.of(2026, 4, 8, 20, 0));
            product.setUpdatedAt(LocalDateTime.of(2026, 4, 8, 20, 5));
            return product;
        });

        ProductResponse response = productService.create(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();

        assertEquals("Phone", savedProduct.getName());
        assertEquals("imran", savedProduct.getCreatedBy().getUsername());
        assertEquals(11L, response.getId());
        assertEquals("imran", response.getCreatedBy());
        assertEquals("Electronics", response.getCategory());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getUpdatedAt());
    }

    @Test
    void getById_shouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getById(99L)
        );

        assertEquals("Product not found with id: 99", exception.getMessage());
    }

    @Test
    void update_shouldOverwriteMutableFieldsAndKeepCreator() {
        Product existingProduct = new Product();
        existingProduct.setId(5L);
        existingProduct.setName("Old Name");
        existingProduct.setDescription("Old Description");
        existingProduct.setPrice(10.0);
        existingProduct.setQuantity(1);
        existingProduct.setCategory("Old");
        User owner = new User();
        owner.setUsername("owner");
        existingProduct.setCreatedBy(owner);

        ProductRequest request = buildRequest();

        when(productRepository.findById(5L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        ProductResponse response = productService.update(5L, request);

        assertEquals("Phone", existingProduct.getName());
        assertEquals("Latest model", existingProduct.getDescription());
        assertEquals(999.0, existingProduct.getPrice());
        assertEquals(10, existingProduct.getQuantity());
        assertEquals("Electronics", existingProduct.getCategory());
        assertEquals("owner", existingProduct.getCreatedBy().getUsername());
        assertEquals("owner", response.getCreatedBy());
    }

    @Test
    void delete_shouldThrowWhenProductDoesNotExist() {
        when(productRepository.existsById(42L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productService.delete(42L)
        );

        assertEquals("Product not found with id: 42", exception.getMessage());
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void getAll_shouldMapNullCreatorWithoutFailure() {
        Product product = new Product();
        product.setId(3L);
        product.setName("Notebook");
        product.setDescription("Paper notebook");
        product.setPrice(5.0);
        product.setQuantity(20);
        product.setCategory("Stationery");
        product.setCreatedBy(null);

        when(productRepository.findAll()).thenReturn(java.util.List.of(product));

        ProductResponse response = productService.getAll().getFirst();

        assertEquals(3L, response.getId());
        assertEquals("Notebook", response.getName());
        assertNull(response.getCreatedBy());
    }

    private ProductRequest buildRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Phone");
        request.setDescription("Latest model");
        request.setPrice(999.0);
        request.setQuantity(10);
        request.setCategory("Electronics");
        return request;
    }
}
