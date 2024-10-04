package br.ufpr.tads.catalog.catalog.application;

import br.ufpr.tads.catalog.catalog.domain.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<?> getProducts(@RequestParam("page") int page, @RequestParam("size") int size,
                                         @RequestParam("sortDirection") Sort.Direction sortDirection, @RequestParam("sortBy") String sortBy) {
        try {
            log.info("Getting products");
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(productService.getProducts(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @GetMapping("/lowest-price")
    public ResponseEntity<?> findLowestPriceForAllProducts(@RequestParam("page") int page, @RequestParam("size") int size,
                                         @RequestParam("sortDirection") Sort.Direction sortDirection, @RequestParam("sortBy") String sortBy) {
        try {
            log.info("Getting products");
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(productService.findLowestPriceForAllProducts(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> getProducts(@RequestParam("name") String name) {
        try {
            log.info("Searching products by name: {}", name);
            return ResponseEntity.ok(productService.searchProductsByName(name));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @GetMapping("/price-history")
    public ResponseEntity<?> getPriceHistory(@RequestParam("page") int page, @RequestParam("size") int size,
                                             @RequestParam("sortDirection") Sort.Direction sortDirection,
                                             @RequestParam("sortBy") String sortBy,
                                             @RequestParam("id") UUID id) {
        try {
            log.info("Getting price history for product {}", id);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(productService.getPriceHistory(id, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

}
