package br.ufpr.tads.catalog.catalog.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "PRODUCT_STORE")
public class ProductStore {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    @Column(name = "BRANCH_ID", nullable = false)
    private UUID branchId;

    @Column(name = "UNIT", nullable = false)
    private String unit;

    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "productStore", cascade = CascadeType.ALL, orphanRemoval = true)
    private Promotion promotion;

}
