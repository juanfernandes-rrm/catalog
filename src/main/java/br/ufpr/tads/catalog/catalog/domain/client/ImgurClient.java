package br.ufpr.tads.catalog.catalog.domain.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImgurClient {

    @Value("${imgur.upload-url}")
    private String IMGUR_UPLOAD_URL;

    @Value("${imgur.client-id}")
    private String CLIENT_ID;

    public String uploadImage(MultipartFile file) throws Exception {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

        Map<String, String> payload = new HashMap<>();
        payload.put("image", base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Client-ID " + CLIENT_ID);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(IMGUR_UPLOAD_URL, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            return (String) data.get("link");
        } else {
            throw new Exception("Erro ao fazer upload no Imgur: " + response.getStatusCode());
        }
    }
}
