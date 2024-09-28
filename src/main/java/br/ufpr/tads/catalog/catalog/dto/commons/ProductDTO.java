package br.ufpr.tads.catalog.catalog.dto.commons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {

    private UUID id;
    private String name;
    private String code;
    private String category;
    private String image;
    private BigDecimal price;
    private String unit;
    private UUID storeId;

}
