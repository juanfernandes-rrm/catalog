package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.dto.commons.BranchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class RegisterRetriever {

    private static final String REGISTER_SERVICE_URL = "http://localhost:8084";
    private static final String GET_BRANCH_BY_CORRELATION_ID = "/store/branch/%s";
    @Autowired
    private RestTemplate restTemplate;

    public BranchDTO getBranch(UUID correlationId) {
        ResponseEntity<BranchDTO> response = restTemplate.exchange(
                REGISTER_SERVICE_URL + GET_BRANCH_BY_CORRELATION_ID.formatted(correlationId.toString()),
                HttpMethod.GET,
                null,
                BranchDTO.class
        );

        if(response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        }

        return null;
    }
}
