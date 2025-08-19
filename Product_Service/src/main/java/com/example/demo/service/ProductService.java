package com.example.demo.service;


import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.model.Product;

@Service
public interface ProductService {
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    Optional<Product> getProductById(Long id);
    List<Product> getAllProducts();
}
