package br.ufpr.tads.catalog.catalog.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequestDTO {

    @NotBlank(message = "O nome da categoria não pode estar vazio.")
    @Size(max = 50, message = "O nome da categoria deve ter no máximo 50 caracteres.")
    private String name;

    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
    private String description;

}
