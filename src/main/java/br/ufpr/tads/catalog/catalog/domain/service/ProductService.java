package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductStoreRepository;
import br.ufpr.tads.catalog.catalog.domain.response.ItemDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.ProductResponseDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.ProductsDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStoreRepository productStoreRepository;

    public void process(ProductsDTO productsDTO) {
        productsDTO.getItems().forEach(item -> {
            Product product = saveOrUpdateProduct(item);
            Optional<ProductStore> existingProductStore = productStoreRepository.findByProductIdAndBranchId(product.getId(), productsDTO.getBranchId());

            ProductStore productStore;
            if (existingProductStore.isPresent()) {
                productStore = existingProductStore.get();
                if (!productStore.getPrice().equals(item.getUnitValue())) {
                    // Save the current price to history before updating
//                    savePriceHistory(productStore);
                    productStore.setPrice(item.getUnitValue());
                    productStore.setCreatedAt(LocalDateTime.now());
                    productStoreRepository.save(productStore);
                }
            } else {
                productStore = new ProductStore();
                productStore.setProduct(product);
                productStore.setBranchId(productsDTO.getBranchId());
                productStore.setPrice(item.getUnitValue());
                productStore.setUnit(item.getUnit());
                productStore.setCreatedAt(LocalDateTime.now());
                productStoreRepository.save(productStore);

                // Save the initial price to history
//                savePriceHistory(productStore);
            }
        });
    }

//    private void savePriceHistory(ProductStore productStore) {
//        PriceHistory priceHistory = new PriceHistory();
//        priceHistory.setProductStore(productStore);
//        priceHistory.setPrice(productStore.getPrice());
//        priceHistory.setCreatedAt(LocalDateTime.now());
//        priceHistoryRepository.save(priceHistory);
//    }

    public Page<ProductResponseDTO> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        List<ProductResponseDTO> productResponseDTOs = products.stream().map(product -> {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
            return mapProduct(product, productStore);
        }).collect(Collectors.toList());

        return new PageImpl<>(productResponseDTOs, pageable, products.getTotalElements());
    }

    public Page<ProductResponseDTO> findLowestPriceForAllProducts(Pageable pageable) {
        List<ProductResponseDTO> responseDTOS = new ArrayList<>();
        Page<Product> products = productRepository.findAll(pageable);
        products.forEach(product -> {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
            responseDTOS.add(new ProductResponseDTO(product.getId(), product.getName(), product.getCode(), productStore.getPrice(), productStore.getBranchId()));
        });
        return new PageImpl<>(responseDTOS, pageable, products.getTotalElements());
    }

    public ProductResponseDTO searchProductsByName(String name) {
        String treatedName = StringUtils.stripAccents(name).trim();
        List<Product> products = productRepository.findByNameContainingIgnoreCase(treatedName);
        return products.stream()
                .map(product -> {
                    ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
                    return mapProduct(product, productStore);
                })
                .findFirst()
                .orElse(null);
    }

    private ProductResponseDTO mapProduct(Product product, ProductStore productStore) {
        return new ProductResponseDTO(product.getId(), product.getName(), product.getCode(), productStore.getPrice(), productStore.getBranchId());
    }

    private ProductResponseDTO mapProduct(ProductStore productStore) {
        Product product = productStore.getProduct();
        return new ProductResponseDTO(product.getId(), product.getName(), product.getCode(), productStore.getPrice(), productStore.getBranchId());
    }

    private Product saveOrUpdateProduct(ItemDTO itemDTO) {
        return getProduct(itemDTO.getCode())
                .map(existingProduct -> updateProduct(existingProduct, itemDTO))
                .orElseGet(() -> saveProduct(itemDTO));
    }

    private Optional<Product> getProduct(String code) {
        return productRepository.findByCode(code);
    }

    private Product updateProduct(Product product, ItemDTO item) {
        product.setName(item.getName());
        return productRepository.save(product);
    }

    private Product saveProduct(ItemDTO item) {
        Product product = new Product();
        product.setName(item.getName());
        product.setCode(item.getCode());
        return productRepository.save(product);
    }

}
