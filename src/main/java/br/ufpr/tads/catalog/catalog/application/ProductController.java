package br.ufpr.tads.catalog.catalog.application;

import br.ufpr.tads.catalog.catalog.domain.request.ProductsPriceRequestDTO;
import br.ufpr.tads.catalog.catalog.domain.service.PriceService;
import br.ufpr.tads.catalog.catalog.domain.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private PriceService priceService;

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

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable("id") UUID id) {
        try {
            log.info("Getting product {}", id);
            return ResponseEntity.ok(productService.getProductByCode(id));
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
    public ResponseEntity<?> getProducts(@RequestParam("page") int page, @RequestParam("size") int size,
                                         @RequestParam("sortDirection") Sort.Direction sortDirection,
                                         @RequestParam("sortBy") String sortBy,
                                         @RequestParam("name") String name) {
        try {
            log.info("Searching products by name: {}", name);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(productService.searchProductsByName(name, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping("/products-details")
    public ResponseEntity<?> getProductsDetails(@RequestBody List<UUID> productIdList,
                                                @RequestParam("page") int page, @RequestParam("size") int size,
                                                @RequestParam("sortDirection") Sort.Direction sortDirection,
                                                @RequestParam("sortBy") String sortBy) {
        try {
            log.info("Searching products details by id list: {}", productIdList);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(productService.getProductsDetails(productIdList, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PostMapping("/prices-by-store")
    public ResponseEntity<?> getProductsPriceByStore(@RequestBody ProductsPriceRequestDTO productsPriceRequestDTO,
                                                     @RequestParam("page") int page, @RequestParam("size") int size,
                                                     @RequestParam("sortDirection") Sort.Direction sortDirection,
                                                     @RequestParam("sortBy") String sortBy) {
        try {
            log.info("Searching products price by stores {}, for cep {} in distance {}",
                    productsPriceRequestDTO.getProducts(), productsPriceRequestDTO.getCep(), productsPriceRequestDTO.getDistance());
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(priceService.calculateTotalPriceByStore(productsPriceRequestDTO, pageable));
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
            return ResponseEntity.ok(priceService.getPriceHistory(id, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

}
