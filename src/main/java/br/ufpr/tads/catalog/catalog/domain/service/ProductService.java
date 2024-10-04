package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.domain.model.PriceHistory;
import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.repository.PriceHistoryRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductStoreRepository;
import br.ufpr.tads.catalog.catalog.domain.response.ItemDTO;
import br.ufpr.tads.catalog.catalog.domain.response.PriceHistoryResponseDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.ProductResponseDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.ProductsDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    public void process(ProductsDTO productsDTO) {
        productsDTO.getItems().forEach(item -> {
            Product product = saveOrUpdateProduct(item);
            ProductStore productStore = getOrCreateProductStore(productsDTO, item, product);
            updatePriceIfNecessary(item, productStore);
        });
    }

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

    public PriceHistoryResponseDTO getPriceHistory(UUID productId, Pageable pageable) {
        Slice<PriceHistory> priceHistorySlice = priceHistoryRepository.findByProductId(productId, pageable);

        PriceHistoryResponseDTO response = new PriceHistoryResponseDTO();
        if(priceHistorySlice.hasContent()){
            Product product = priceHistorySlice.getContent().stream().findFirst().get().getProductStore().getProduct();
            response.setProductId(product.getId());
            response.setProductName(product.getName());
            response.setProductCode(product.getCode());
            response.setCategoryName(nonNull(product.getCategory()) ? product.getCategory().getName() : null);

            List<PriceHistoryResponseDTO.PriceHistoryByStore> historyList = priceHistorySlice.getContent().stream()
                    .map(priceHistory -> {
                        PriceHistoryResponseDTO.PriceHistoryByStore priceHistoryByStoreDTO = new PriceHistoryResponseDTO.PriceHistoryByStore();
                        priceHistoryByStoreDTO.setStoreId(priceHistory.getProductStore().getBranchId());
                        priceHistoryByStoreDTO.setPrice(priceHistory.getPrice());
                        priceHistoryByStoreDTO.setPriceChangeDate(priceHistory.getCreatedAt());

                        return priceHistoryByStoreDTO;
                    }).toList();

            response.setPriceHistory(historyList);

            PriceHistoryResponseDTO.SliceInfo sliceInfo = new PriceHistoryResponseDTO.SliceInfo();
            sliceInfo.setHasNext(priceHistorySlice.hasNext());
            sliceInfo.setHasPrevious(priceHistorySlice.hasPrevious());
            sliceInfo.setPageSize(priceHistorySlice.getSize());
            response.setSliceInfo(sliceInfo);
        }

        return response;
    }

    private ProductStore getOrCreateProductStore(ProductsDTO productsDTO, ItemDTO item, Product product) {
        return productStoreRepository.findByProductIdAndBranchId(product.getId(), productsDTO.getBranchId())
                .orElseGet(() -> createAndSaveProductStore(productsDTO, item, product));
    }

    private ProductStore createAndSaveProductStore(ProductsDTO productsDTO, ItemDTO item, Product product) {
        ProductStore productStore = new ProductStore();
        productStore.setProduct(product);
        productStore.setBranchId(productsDTO.getBranchId());
        productStore.setPrice(item.getUnitValue());
        productStore.setUnit(item.getUnit());
        productStore.setCreatedAt(LocalDateTime.now());
        return productStoreRepository.save(productStore);
    }

    private void updatePriceIfNecessary(ItemDTO item, ProductStore productStore) {
        if (!productStore.getPrice().equals(item.getUnitValue())) {
            saveProductHistory(productStore);
            productStore.setPrice(item.getUnitValue());
            productStore.setCreatedAt(LocalDateTime.now());
            productStoreRepository.save(productStore);
        }
    }

    private void saveProductHistory(ProductStore productStore) {
        PriceHistory history = new PriceHistory();
        history.setProductStore(productStore);
        history.setPrice(productStore.getPrice());
        history.setCreatedAt(LocalDateTime.now());

        priceHistoryRepository.save(history);
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
