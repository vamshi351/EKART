package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "products",
       indexes = {
           @Index(name = "idx_product_sku", columnList = "sku", unique = true),
           @Index(name = "idx_product_category", columnList = "category"),
           @Index(name = "idx_product_brand", columnList = "brand"),
           @Index(name = "idx_product_active", columnList = "active"),
           @Index(name = "idx_product_slug", columnList = "slug")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200, message = "Product name must be between 2 and 200 characters")
    @Column(nullable = false)
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "SKU is required")
    @Size(min = 3, max = 100, message = "SKU must be between 3 and 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(nullable = false)
    private Double price;

    @DecimalMin(value = "0.0", message = "Sale price must be greater than or equal to 0")
    private Double salePrice;

    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
    @Column(nullable = false)
    private Integer stockQuantity;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    private String imageUrl;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    @Size(max = 20, message = "Unit of measure must not exceed 20 characters")
    private String unitOfMeasure;

    @DecimalMin(value = "0.0", message = "Weight must be greater than or equal to 0")
    private Double weight;
    
    @DecimalMin(value = "0.0", message = "Height must be greater than or equal to 0")
    private Double height;
    
    @DecimalMin(value = "0.0", message = "Width must be greater than or equal to 0")
    private Double width;
    
    @DecimalMin(value = "0.0", message = "Depth must be greater than or equal to 0")
    private Double depth;

    @Size(max = 200, message = "Slug must not exceed 200 characters")
    private String slug;

    @Size(max = 200, message = "Meta title must not exceed 200 characters")
    private String metaTitle;
    
    @Size(max = 300, message = "Meta description must not exceed 300 characters")
    private String metaDescription;

    private boolean active = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    @Version
    private Long version;
}
