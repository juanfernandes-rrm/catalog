package br.ufpr.tads.catalog.catalog.domain.response.commons;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
public class ReceiptResponseDTO {
    private StoreDTO store;
    private List<ItemDTO> items;
    private Integer totalItems;
    private BigDecimal totalValue;
    private String paymentMethod;
    private BigDecimal valuePaid;
    private BigDecimal tax;
    private GeneralInformationDTO generalInformation;
    private String accessKey;
}