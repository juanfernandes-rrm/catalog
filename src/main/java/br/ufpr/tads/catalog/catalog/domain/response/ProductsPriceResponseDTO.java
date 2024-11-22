package br.ufpr.tads.catalog.catalog.domain.response;

import br.ufpr.tads.catalog.catalog.dto.commons.BranchDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
public class ProductsPriceResponseDTO {

    private BigDecimal totalPrice;
    private int productQuantity;
    private BranchDTO branch;
    private List<ProductItemResponseDTO> products;

}
