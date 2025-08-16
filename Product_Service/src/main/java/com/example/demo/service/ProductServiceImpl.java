package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product createProduct(Product product) {
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
                    existingProduct.setSlug(updatedProduct.getSlug());
                    existingProduct.setMetaTitle(updatedProduct.getMetaTitle());
                    existingProduct.setMetaDescription(updatedProduct.getMetaDescription());
                    existingProduct.setActive(updatedProduct.isActive());
                    return productRepository.save(existingProduct);
                })
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Override
    public void deleteProduct(Long id) {
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
}
