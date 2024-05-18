package br.ufpr.tads.catalog.catalog.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreDTO {
    private String name;
    private String CNPJ;
    private AddressDTO address;
}
