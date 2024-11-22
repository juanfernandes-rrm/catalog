package br.ufpr.tads.catalog.catalog.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductItemRequestDTO {

    private UUID productId;
    private int quantity;

}
