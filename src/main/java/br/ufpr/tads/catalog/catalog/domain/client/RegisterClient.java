package br.ufpr.tads.catalog.catalog.domain.client;

import br.ufpr.tads.catalog.catalog.dto.commons.BranchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class RegisterClient {

    private static final String REGISTER_SERVICE_URL = "http://localhost:8084";
    private static final String GET_BRANCH_BY_CORRELATION_ID = "/store/branch/%s";
    private static final String GET_NEARBY_BRANCHES_BY_CEP = "/store/branch/nearby-from/%s?distance=%s";

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

    public List<BranchDTO> getNearbyBranches(String cep, double distance) {
        String url = REGISTER_SERVICE_URL + GET_NEARBY_BRANCHES_BY_CEP.formatted(cep, distance);

        try {
            ResponseEntity<List<BranchDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            throw new RuntimeException("Falha ao buscar as lojas próximas: resposta vazia ou status inválido.");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar ao serviço de lojas próximas: " + e.getMessage(), e);
        }
    }
}
