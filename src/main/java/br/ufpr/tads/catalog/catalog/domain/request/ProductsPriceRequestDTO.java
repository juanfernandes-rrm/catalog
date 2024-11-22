package br.ufpr.tads.catalog.catalog.domain.request;

import lombok.Data;

import java.util.List;

@Data
public class ProductsPriceRequestDTO {
    private List<ProductItemRequestDTO> products;
    private String cep;
    private double distance;
}
