package br.ufpr.tads.catalog.catalog.dto.commons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private String street;
    private String number;
    private String neighborhood;
    private String city;
    private String state;

}
