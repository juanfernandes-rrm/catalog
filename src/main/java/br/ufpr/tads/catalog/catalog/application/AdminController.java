package br.ufpr.tads.catalog.catalog.application;

import br.ufpr.tads.catalog.catalog.domain.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @GetMapping("/total-registered-products")
    public ResponseEntity<?> getTotalRegisteredProducts() {
        try {
            return ResponseEntity.ok(productService.getTotalRegisteredProducts());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }
}
