package br.ufpr.tads.catalog.catalog.application;

import br.ufpr.tads.catalog.catalog.domain.request.CreateCategoryRequestDTO;
import br.ufpr.tads.catalog.catalog.domain.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CreateCategoryRequestDTO createCategoryRequestDTO) {
        log.info("Creating category");
        try {
            return ResponseEntity.ok(categoryService.createCategory(createCategoryRequestDTO));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getCategories(@RequestParam("page") int page, @RequestParam("size") int size,
                                           @RequestParam("sortDirection") Sort.Direction sortDirection,
                                           @RequestParam("sortBy") String sortBy) {
        try {
            log.info("Getting categories");
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            return ResponseEntity.ok(categoryService.getCategories(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategories(@PathVariable("id") Long id) {
        try {
            log.info("Deleting category {}", id);
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategories(@PathVariable("id") Long id, @RequestBody CreateCategoryRequestDTO createCategoryRequestDTO) {
        try {
            log.info("Updating category {}", id);
            return ResponseEntity.ok(categoryService.updateCategory(id, createCategoryRequestDTO));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }

}
