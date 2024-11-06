package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.domain.model.PriceHistory;
import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.repository.PriceHistoryRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductStoreRepository;
import br.ufpr.tads.catalog.catalog.domain.response.GetProductResponseDTO;
import br.ufpr.tads.catalog.catalog.domain.response.PriceHistoryResponseDTO;
import br.ufpr.tads.catalog.catalog.domain.response.commons.ItemDTO;
import br.ufpr.tads.catalog.catalog.domain.response.commons.ProductDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.BranchDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.ProductsDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
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

    @Autowired
    private RegisterRetriever registerRetriever;

    public void process(ProductsDTO productsDTO) {
        productsDTO.getItems().forEach(item -> {
            Product product = saveOrUpdateProduct(item);
            ProductStore productStore = getOrCreateProductStore(productsDTO, item, product);
            updatePriceIfNecessary(item, productStore);
        });
    }

    public Page<ProductDTO> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        List<ProductDTO> productDTOList = products.stream().map(product -> {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
            return createProductDTO(product, productStore);
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

    //TODO: talvez usar projection para poder filtar por campos que são realmente retornados
    public SliceImpl<ProductDTO> searchProductsByName(String name, Pageable pageable) {
        String treatedName = StringUtils.stripAccents(name).trim();
        Slice<Product> products = productRepository.findByNameContainingIgnoreCase(treatedName, pageable);

        List<ProductDTO> response = products.stream()
                .map(product -> {
                    ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
                    return createProductDTO(product, productStore);
                })
                .toList();
        return new SliceImpl<>(response, pageable, products.hasNext());
    }

    public SliceImpl<PriceHistoryResponseDTO> getPriceHistory(UUID productId, Pageable pageable) {
        Slice<PriceHistory> priceHistorySlice = priceHistoryRepository.findByProductId(productId, pageable);

        List<PriceHistoryResponseDTO> responses = new ArrayList<>();
        if (priceHistorySlice.hasContent()) {
            responses = priceHistorySlice.getContent().stream().map(priceHistory -> {
                ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(priceHistory.getProductStore().getProduct().getId());
                PriceHistoryResponseDTO responseDTO = new PriceHistoryResponseDTO();
                responseDTO.setStoreId(productStore.getBranchId());
                BranchDTO branch = registerRetriever.getBranch(productStore.getBranchId());
                responseDTO.setStoreName(nonNull(branch) ? branch.getStore().getName() : "Loja não encontrada");
                responseDTO.setBranchId(branch.getId());
                responseDTO.setPrice(priceHistory.getPrice());
                responseDTO.setPriceChangeDate(priceHistory.getCreatedAt());
                return responseDTO;
            }).toList();
        }

        return new SliceImpl<>(responses, pageable, priceHistorySlice.hasNext());
    }

    public SliceImpl<ProductDTO> getProductsDetails(List<UUID> productIdList, Pageable pageable) {
        List<ProductDTO> responseDTOS = new ArrayList<>();
        Page<Product> products = productRepository.findAllByIdIn(productIdList, pageable);
        products.forEach(product -> {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
            responseDTOS.add(createProductDTO(product, productStore));
        });
        return new SliceImpl<>(responseDTOS, pageable, products.hasNext());
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

    private ProductDTO createProductDTO(Product product, ProductStore productStore) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setCode(product.getCode());
        productDTO.setPrice(productStore.getPrice());
        productDTO.setStoreId(productStore.getBranchId());
        return productDTO;
    }

}
