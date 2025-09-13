package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.config.UserClient;
import com.example.demo.dto.UserDTO;
import com.example.demo.model.Product;
import com.example.demo.service.ProductServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductServiceImpl productService;
    private final UserClient userClient;
    private final HttpServletRequest request; // Injected

    public ProductController(ProductServiceImpl productService, UserClient userClient, HttpServletRequest request) {
        this.productService = productService;
        this.userClient = userClient;
        this.request = request;
    }

    // SELLER or ADMIN can create
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @PostMapping("/create") 
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }

            product.setCreatedBy(auth.getName());
            Product createdProduct = productService.createProduct(product);
            return ResponseEntity.ok(createdProduct);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating product: " + e.getMessage());
        }
    }

    // ANYONE can view all products
    @GetMapping("/allProducts")
    public ResponseEntity<?> getAllProducts() {
        try {
        	System.err.println("Entered inside product-service");
            return ResponseEntity.ok(productService.getAllProducts());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching products: " + e.getMessage());
        }
    }

    // ANYONE can view product by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            Optional<Product> product = productService.getProductById(id);
            return product.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching product: " + e.getMessage());
        }
    }

    // SELLER can update own product, ADMIN can update any
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @Valid @RequestBody Product product) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = auth.getName();

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); // exact ROLE_ prefix check

            Optional<Product> existingProduct = productService.getProductById(id);
            if (existingProduct.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }

            // Admin can always update, seller only their own
            if (!isAdmin && !existingProduct.get().getCreatedBy().equals(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to update this product");
            }

            product.setLastModifiedBy(currentUser);
            Product updatedProduct = productService.updateProduct(id, product);
            return ResponseEntity.ok(updatedProduct);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product: " + e.getMessage());
        }
    }

    // SELLER can delete own product, ADMIN can delete any
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUser = auth.getName();

            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); // Check with ROLE_ prefix

            Optional<Product> existingProduct = productService.getProductById(id);
            if (existingProduct.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
            }

            // Admin can always delete, seller only their own
            if (!isAdmin && !existingProduct.get().getCreatedBy().equals(currentUser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this product");
            }

            productService.deleteProduct(id);
            return ResponseEntity.ok("Product deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting product: " + e.getMessage());
        }
    }

    // SELLER can view own products, ADMIN can view all sellers' products
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<?> getMyProducts(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }

            String currentUser = authentication.getName();

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")); // ROLE_ prefix is mandatory!

            List<Product> myProducts;
            if (isAdmin) {
                myProducts = productService.getAllProducts(); // Admin sees all products
            } else {
                myProducts = productService.getProductsByCreatedBy(currentUser);
            }

            UserDTO userInfo = userClient.getMe("Bearer " + getCurrentToken());

            return ResponseEntity.ok(
                    java.util.Map.of(
                            "user", userInfo,
                            "products", myProducts,
                            "total", myProducts.size()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Extract current JWT token from SecurityContext - implemented properly
     */
    
    

    private String getCurrentToken() {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
