package br.ufpr.tads.catalog.catalog.dto.commons;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponseDTO(UUID id, String name, String code, BigDecimal price, UUID storeCorrelationId) {
}
