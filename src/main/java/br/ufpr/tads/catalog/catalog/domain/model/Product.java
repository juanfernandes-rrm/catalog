package br.ufpr.tads.catalog.catalog.domain.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "PRODUCT")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String code;
    @ManyToOne
    private Category category;
    @OneToMany(mappedBy = "product")
    private Set<ProductStore> productStore;

}
