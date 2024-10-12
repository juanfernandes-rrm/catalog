package br.ufpr.tads.catalog.catalog.domain.response.commons;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDTO {
    private String name;
    private String code;
    private BigDecimal amount;
    private String unit;
    private BigDecimal unitValue;
    private BigDecimal totalValue;
}
