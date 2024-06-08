package br.ufpr.tads.catalog.catalog.dto.commons;

import br.ufpr.tads.catalog.catalog.domain.response.ItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductsDTO {
    private List<ItemDTO> items;
    private UUID branchId;
}
