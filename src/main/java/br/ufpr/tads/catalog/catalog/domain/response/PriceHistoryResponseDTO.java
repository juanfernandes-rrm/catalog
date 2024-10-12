package br.ufpr.tads.catalog.catalog.domain.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PriceHistoryResponseDTO {

    private UUID storeId;
    private UUID branchId;
    private String storeName;
    private String neighborhood;
    private BigDecimal price;
    private LocalDateTime priceChangeDate;

}
