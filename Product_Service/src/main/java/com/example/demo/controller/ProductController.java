package com.example.demo.controller;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.UserClient;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final UserClient userClient;
    private Map<Long, Product> productRepo = new HashMap<>();

    public ProductController(ProductService productService, UserClient userClient) {
        this.productService = productService;
        this.userClient = userClient;
    }

    // Create product - SELLER or ADMIN
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    	System.err.println("entered into fro creation");
        product.setId(new Random().nextLong());
        productRepo.put(product.getId(), product);
        return ResponseEntity.ok(product);
    }



    // Update product - SELLER or ADMIN
    @PutMapping("/updateproduct/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    // Delete product - ADMIN only
    @DeleteMapping("/product/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // Get product by ID - All roles
    @GetMapping("/getproductbyid/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get all products - All roles
    @GetMapping("/getallproducts")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}
