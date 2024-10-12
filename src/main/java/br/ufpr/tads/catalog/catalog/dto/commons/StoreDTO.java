package br.ufpr.tads.catalog.catalog.dto.commons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreDTO {
    private UUID id;
    private String name;
    private AddressDTO address;
    private String cnpj;
}
