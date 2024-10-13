package br.ufpr.tads.catalog.catalog.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

//TODO: ajustar diagrama de classe
@Data
@Entity
@Table(name = "PRICE_HISTORY")
public class PriceHistory {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_STORE_ID", nullable = false)
    private ProductStore productStore;

    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
}
