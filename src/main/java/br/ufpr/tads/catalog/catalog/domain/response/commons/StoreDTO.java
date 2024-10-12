package br.ufpr.tads.catalog.catalog.domain.response.commons;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//TODO: passar para modulo commons
public class StoreDTO {
    private String name;
    private String CNPJ;
    private AddressDTO address;
}
