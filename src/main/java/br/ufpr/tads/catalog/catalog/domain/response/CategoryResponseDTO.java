package br.ufpr.tads.catalog.catalog.domain.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String urlImage;
}
