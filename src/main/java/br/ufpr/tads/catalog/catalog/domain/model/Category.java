package br.ufpr.tads.catalog.catalog.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "CATEGORY")
public class Category {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "URL_IMAGE")
    private String urlImage;

}
