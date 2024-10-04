package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductStoreRepository;
import br.ufpr.tads.catalog.catalog.domain.response.ItemDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.GetProductResponseDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.ProductDTO;
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
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

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

    //Pesquisa de produtos

    public Page<ProductDTO> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        List<ProductDTO> productDTOList = products.stream().map(product -> {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
            return mapProduct(product, productStore);
        }).collect(Collectors.toList());

        return new PageImpl<>(productDTOList, pageable, products.getTotalElements());
    }

    public GetProductResponseDTO getProductById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
        ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
        ProductDTO productDTO = ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .code(product.getCode())
                .category(nonNull(product.getCategory()) ? product.getCategory().getName() : null)
                .price(productStore.getPrice())
                .unit(productStore.getUnit())
                .storeId(productStore.getBranchId())
                .build();
        return new GetProductResponseDTO(productDTO);
    }

    public Page<ProductDTO> findLowestPriceForAllProducts(Pageable pageable) {
        List<ProductDTO> responseDTOS = new ArrayList<>();
        Page<Product> products = productRepository.findAll(pageable);
        products.forEach(product -> {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
            responseDTOS.add(createProductDTO(product, productStore));
        });
        return new PageImpl<>(responseDTOS, pageable, products.getTotalElements());
    }

    public ProductDTO searchProductsByName(String name) {
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

    private ProductDTO createProductDTO(Product product, ProductStore productStore){
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setCode(product.getCode());
        productDTO.setPrice(productStore.getPrice());
        productDTO.setStoreId(productStore.getBranchId());
        return productDTO;
    }

    private ProductDTO mapProduct(Product product, ProductStore productStore) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .code(product.getCode())
                .price(productStore.getPrice())
                .unit(productStore.getUnit())
                .storeId(productStore.getBranchId())
                .build();
    }

    private Product saveOrUpdateProduct(ItemDTO itemDTO) {
        return getProductById(itemDTO.getCode())
                .map(existingProduct -> updateProduct(existingProduct, itemDTO))
                .orElseGet(() -> saveProduct(itemDTO));
    }

    private Optional<Product> getProductById(String code) {
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
