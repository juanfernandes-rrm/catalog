package br.ufpr.tads.catalog.catalog.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IssuanceDTO {
    private LocalDateTime date;
    private String issuer;
}
