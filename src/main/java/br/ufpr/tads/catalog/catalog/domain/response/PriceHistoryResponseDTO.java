package br.ufpr.tads.catalog.catalog.domain.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PriceHistoryResponseDTO {

    private UUID productId;
    private String productName;
    private String productCode;
    private String categoryName;

    private List<PriceHistoryByStore> priceHistory;

    private SliceInfo sliceInfo;


    @Data
    public static class PriceHistoryByStore {
        private UUID storeId;
        private String storeName;
        private BigDecimal price;
        private LocalDateTime priceChangeDate;
    }

    @Data
    public static class SliceInfo {
        private boolean hasNext;
        private boolean hasPrevious;
        private int pageSize;
    }
}
