package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.UserClient;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserClient userClient;

    public ProductController(ProductService productService, UserClient userClient) {
        this.productService = productService;
        this.userClient = userClient;
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        try {
            System.out.println("=== Product Creation Request ===");
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                System.err.println("No authentication found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }
            
            System.out.println("Authentication: " + auth.getName());
            System.out.println("Authorities: " + auth.getAuthorities());
            
            // Set creator information
            product.setCreatedBy(auth.getName());
            
            // Create the product using service
            Product createdProduct = productService.createProduct(product);
            
            System.out.println("Product created successfully with ID: " + createdProduct.getId());
            return ResponseEntity.ok(createdProduct);
            
        } catch (Exception e) {
            System.err.println("Error creating product: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching products: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> product = productService.getProductById(id);
            if (product.isPresent()) {
                return ResponseEntity.ok(product.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching product: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            product.setLastModifiedBy(auth.getName());
            
            Product updatedProduct = productService.updateProduct(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting product: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProducts() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }
            
            // Get user info
            UserDTO userInfo = userClient.getMe("Bearer " + getCurrentToken());
            return ResponseEntity.ok(Map.of(
                "user", userInfo,
                "message", "Product service accessible",
                "authorities", auth.getAuthorities()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    private String getCurrentToken() {
        // This would be better implemented by storing token in request context
        // For now, this is a placeholder - you might need to modify based on your architecture
        return "";
    }
}
