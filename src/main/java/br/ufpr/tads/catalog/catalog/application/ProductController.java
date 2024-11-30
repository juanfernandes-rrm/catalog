package br.ufpr.tads.catalog.catalog.application;

import br.ufpr.tads.catalog.catalog.domain.request.AddCategoryToProductRequestDTO;
import br.ufpr.tads.catalog.catalog.domain.request.ProductsPriceRequestDTO;
import br.ufpr.tads.catalog.catalog.domain.service.PriceService;
import br.ufpr.tads.catalog.catalog.domain.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<?> getProductById(@PathVariable("id") UUID id,
                                            @RequestParam(value = "includeStore", required = false, defaultValue = "false") boolean includeStore) {
        try {
            log.info("Getting product {}", id);
            return ResponseEntity.ok(productService.getProductById(id, includeStore));
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

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{productId}/add-category")
    public ResponseEntity<?> addCategory(@PathVariable("productId") UUID productId, @RequestBody AddCategoryToProductRequestDTO addCategoryToProductRequestDTO) {
        try {
            log.info("Add category {} to product {}", addCategoryToProductRequestDTO.getCategoryId(), productId);
            return ResponseEntity.ok(productService.addCategory(addCategoryToProductRequestDTO));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }


    //TODO: esses endpoins podem ser substituídos por um endpoint de busca com filtros
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/without-category")
    public ResponseEntity<?> getProductsWithoutCategory(@RequestParam("page") int page, @RequestParam("size") int size,
                                                        @RequestParam("sortDirection") Sort.Direction sortDirection,
                                                        @RequestParam("sortBy") String sortBy) {
        try {
            log.info("Getting products without category");
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(productService.getProductsWithoutCategory(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/without-image")
    public ResponseEntity<?> getProductsWithoutImage(@RequestParam("page") int page, @RequestParam("size") int size,
                                                     @RequestParam("sortDirection") Sort.Direction sortDirection,
                                                     @RequestParam("sortBy") String sortBy) {
        try {
            log.info("Getting products without category");
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(productService.getProductsWithoutImage(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @GetMapping("/by-category")
    public ResponseEntity<?> getProductsByCategory(@RequestParam("categoryId") Long categoryId,
                                                   @RequestParam("page") int page, @RequestParam("size") int size,
                                                   @RequestParam("sortDirection") Sort.Direction sortDirection,
                                                   @RequestParam("sortBy") String sortBy) {
        try {
            log.info("Getting products by category {}", categoryId);
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("{productId}/upload-image")
    public ResponseEntity<?> uploadImage(@PathVariable UUID productId, @RequestParam("image") MultipartFile image) {
        try {
            log.info("Uploading image for product {}", productId);
            productService.uploadImage(productId, image);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao fazer upload: " + e.getMessage());
        }
    }

}
