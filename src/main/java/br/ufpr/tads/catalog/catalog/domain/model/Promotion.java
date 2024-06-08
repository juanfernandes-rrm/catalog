package br.ufpr.tads.catalog.catalog.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "PROMOTION")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "PRODUCT_STORE_ID", nullable = false)
    private ProductStore productStore;

    @Column(nullable = false)
    private BigDecimal promotionalPrice;

    private LocalDateTime promotionStart;

    private LocalDateTime promotionEnd;
}
