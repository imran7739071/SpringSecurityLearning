package com.imri.spring_security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imri.spring_security.config.SecurityConfig;
import com.imri.spring_security.dto.ProductRequest;
import com.imri.spring_security.dto.ProductResponse;
import com.imri.spring_security.exception.GlobalExceptionHandler;
import com.imri.spring_security.exception.ResourceNotFoundException;
import com.imri.spring_security.security.JwtAuthFilter;
import com.imri.spring_security.security.OAuth2LoginSuccessHandler;
import com.imri.spring_security.security.UserDetailsServiceImpl;
import com.imri.spring_security.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    @WithMockUser(roles = "USER")
    void getAll_shouldReturnProducts() throws Exception {
        when(productService.getAll()).thenReturn(List.of(buildProductResponse()));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Phone"))
                .andExpect(jsonPath("$[0].createdBy").value("imran"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_shouldReturnCreatedForManager() throws Exception {
        ProductRequest request = buildValidRequest();
        ProductResponse response = buildProductResponse();

        when(productService.create(any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("Electronics"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_shouldReturnServerErrorWhenAccessIsDeniedByExceptionHandler() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidRequest())))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong: Access Denied"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturnUpdatedProduct() throws Exception {
        ProductResponse response = buildProductResponse();
        response.setName("Updated Phone");

        when(productService.update(eq(1L), any(ProductRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Phone"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturnSuccessMessage() throws Exception {
        doNothing().when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Product deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getById_shouldReturnNotFoundWhenServiceThrows() throws Exception {
        when(productService.getById(99L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 99"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: 99"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturnBadRequestForInvalidPayload() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setDescription("Invalid");
        request.setPrice(0.0);
        request.setQuantity(-1);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Product name is required"))
                .andExpect(jsonPath("$.errors.price").value("Price must be greater than 0"))
                .andExpect(jsonPath("$.errors.quantity").value("Quantity cannot be negative"))
                .andExpect(jsonPath("$.errors.category").value("Category is required"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void delete_shouldReturnServerErrorWhenAccessIsDeniedByExceptionHandler() throws Exception {
        doThrow(new AssertionError("service should not be called")).when(productService).delete(1L);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong: Access Denied"));
    }

    private ProductRequest buildValidRequest() {
        ProductRequest request = new ProductRequest();
        request.setName("Phone");
        request.setDescription("Latest model");
        request.setPrice(999.0);
        request.setQuantity(10);
        request.setCategory("Electronics");
        return request;
    }

    private ProductResponse buildProductResponse() {
        return ProductResponse.builder()
                .id(1L)
                .name("Phone")
                .description("Latest model")
                .price(999.0)
                .quantity(10)
                .category("Electronics")
                .createdBy("imran")
                .createdAt(LocalDateTime.of(2026, 4, 8, 20, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 8, 20, 5))
                .build();
    }
}
