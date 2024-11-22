package br.ufpr.tads.catalog.catalog.dto.commons;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

//TODO: jogar para modulo commons
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BranchDTO {
    private UUID id;
    private UUID correlationId;
    private StoreDTO store;
    private double distance;
}
