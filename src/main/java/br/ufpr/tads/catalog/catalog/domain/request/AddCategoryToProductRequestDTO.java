package br.ufpr.tads.catalog.catalog.domain.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCategoryToProductRequestDTO {

    @NotEmpty(message = "productId is required")
    private UUID productId;

    @NotEmpty(message = "categoryId is required")
    private Long categoryId;

}
