package br.ufpr.tads.catalog.catalog.domain.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuthorizationProtocolDTO {
    private String code;
    private LocalDateTime date;
}
