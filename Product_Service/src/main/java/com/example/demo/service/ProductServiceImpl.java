package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product createProduct(Product product) {
        // Generate slug if not provided
        if (product.getSlug() == null || product.getSlug().isEmpty()) {
            product.setSlug(generateSlug(product.getName()));
        }
        
        // Set creation timestamp
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product updatedProduct) {
        return productRepository.findById(id)
                .map(existingProduct -> {
                    existingProduct.setName(updatedProduct.getName());
                    existingProduct.setDescription(updatedProduct.getDescription());
                    existingProduct.setPrice(updatedProduct.getPrice());
                    existingProduct.setSalePrice(updatedProduct.getSalePrice());
                    existingProduct.setSaleStartDate(updatedProduct.getSaleStartDate());
                    existingProduct.setSaleEndDate(updatedProduct.getSaleEndDate());
                    existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
                    existingProduct.setBrand(updatedProduct.getBrand());
                    existingProduct.setCategory(updatedProduct.getCategory());
                    existingProduct.setImageUrl(updatedProduct.getImageUrl());
                    existingProduct.setImageUrls(updatedProduct.getImageUrls());
                    existingProduct.setUnitOfMeasure(updatedProduct.getUnitOfMeasure());
                    existingProduct.setWeight(updatedProduct.getWeight());
                    existingProduct.setHeight(updatedProduct.getHeight());
                    existingProduct.setWidth(updatedProduct.getWidth());
                    existingProduct.setDepth(updatedProduct.getDepth());
                    
                    // Update slug if name changed
                    if (!existingProduct.getName().equals(updatedProduct.getName())) {
                        existingProduct.setSlug(generateSlug(updatedProduct.getName()));
                    }
                    
                    existingProduct.setMetaTitle(updatedProduct.getMetaTitle());
                    existingProduct.setMetaDescription(updatedProduct.getMetaDescription());
                    existingProduct.setActive(updatedProduct.isActive());
                    existingProduct.setUpdatedAt(LocalDateTime.now());
                    existingProduct.setLastModifiedBy(updatedProduct.getLastModifiedBy());
                    
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9\\s]", "")
                   .replaceAll("\\s+", "-")
                   .trim();
    }
}