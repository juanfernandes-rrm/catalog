package br.ufpr.tads.catalog.catalog.domain.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "CATEGORY")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;

}
