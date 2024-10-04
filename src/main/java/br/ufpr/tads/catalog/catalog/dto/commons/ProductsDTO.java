package br.ufpr.tads.catalog.catalog.dto.commons;

import br.ufpr.tads.catalog.catalog.domain.response.ItemDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

//Objeto que o receipt-scan envia para a fila.
//TODO: Refatorar para que importe esse objeto de um pacote comum do receipt-scan

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductsDTO {
    private List<ItemDTO> items;
    private UUID branchId;
}
