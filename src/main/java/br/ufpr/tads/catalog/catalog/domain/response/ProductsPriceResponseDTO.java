package br.ufpr.tads.catalog.catalog.domain.response;

import br.ufpr.tads.catalog.catalog.dto.commons.BranchDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
public class ProductsPriceResponseDTO {

    private BigDecimal totalPrice;
    private int productQuantity;
    private List<UUID> productsId;
    private BranchDTO branch;

}
