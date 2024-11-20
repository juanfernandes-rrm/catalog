package br.ufpr.tads.catalog.catalog.domain.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProductsPriceRequestDTO {
    private List<UUID> productIdList;
    private String cep;
    private double distance;
}
