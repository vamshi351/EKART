package com.example.demo.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false)
    private Double price;

    private Double salePrice;

    private LocalDateTime saleStartDate;
    private LocalDateTime saleEndDate;

    @Column(nullable = false)
    private Integer stockQuantity;

    private String brand;

    private String category;

    private String imageUrl; // Primary image

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls; // Additional images

    private String unitOfMeasure; // e.g., "kg", "piece", "liter"

    private Double weight; // in kg
    private Double height; // in cm
    private Double width;
    private Double depth;

    private String slug; // SEO-friendly URL path

    private String metaTitle;
    private String metaDescription;

    private boolean active = true;

    // Audit metadata
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