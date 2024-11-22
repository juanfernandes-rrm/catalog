package br.ufpr.tads.catalog.catalog.domain.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductItemResponseDTO {

    private UUID productId;
    private int quantity;
    private BigDecimal price;
    private BigDecimal total;

}
