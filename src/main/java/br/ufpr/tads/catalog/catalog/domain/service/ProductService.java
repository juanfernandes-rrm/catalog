package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.domain.model.Category;
import br.ufpr.tads.catalog.catalog.domain.model.PriceHistory;
import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.repository.PriceHistoryRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductStoreRepository;
import br.ufpr.tads.catalog.catalog.domain.request.AddCategoryToProductRequestDTO;
import br.ufpr.tads.catalog.catalog.domain.response.TotalRegisteredProducts;
import br.ufpr.tads.catalog.catalog.domain.response.commons.ItemDTO;
import br.ufpr.tads.catalog.catalog.domain.response.commons.ProductDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.ProductsDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStoreRepository productStoreRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private CategoryService categoryService;

    public void process(ProductsDTO productsDTO) {
        productsDTO.getItems().forEach(item -> {
            Product product = saveOrUpdateProduct(item);
            ProductStore productStore = getOrCreateProductStore(productsDTO, item, product);
            updatePriceIfNecessary(productStore, item.getUnitValue());
        });
    }

    public Page<ProductDTO> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        List<ProductDTO> productDTOList = products.stream().map(product -> {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
            return createProductDTO(product, productStore);
        }).collect(toList());

        return new PageImpl<>(productDTOList, pageable, products.getTotalElements());
    }

    public ProductDTO getProductById(UUID id, boolean includeStore) {
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        ProductDTO.ProductDTOBuilder productDTOBuilder = ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .code(product.getCode())
                .category(nonNull(product.getCategory()) ? product.getCategory().getName() : null)
                .image(product.getUrlImage());

        if (includeStore) {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
            productDTOBuilder.price(productStore.getPrice())
                    .unit(productStore.getUnit())
                    .storeId(productStore.getBranchId());
        }

        return productDTOBuilder.build();
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

    public SliceImpl<ProductDTO> getProductsDetails(List<UUID> productIdList, Pageable pageable) {
        List<ProductDTO> responseDTOS = new ArrayList<>();
        Page<Product> products = productRepository.findAllByIdIn(productIdList, pageable);
        products.forEach(product -> {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId());
            responseDTOS.add(createProductDTO(product, productStore));
        });
        return new SliceImpl<>(responseDTOS, pageable, products.hasNext());
    }

    public TotalRegisteredProducts getTotalRegisteredProducts() {
        return new TotalRegisteredProducts(productRepository.count());
    }

    public SliceImpl<ProductDTO> getProductsWithoutCategory(Pageable pageable) {
        Slice<Product> productPage = productRepository.findByCategoryIsNull(pageable);
        return new SliceImpl<>(productPage.stream().map(this::createProductDTOWithoutStore).toList(),
                productPage.getPageable(), productPage.hasNext());
    }

    public SliceImpl<ProductDTO> getProductsWithoutImage(Pageable pageable) {
        Slice<Product> productPage = productRepository.findByUrlImageIsNull(pageable);
        return new SliceImpl<>(productPage.stream().map(this::createProductDTOWithoutStore).toList(),
                productPage.getPageable(), productPage.hasNext());
    }

    public ProductDTO addCategory(AddCategoryToProductRequestDTO requestDTO) {
        Category category = categoryService.getCategoryById(requestDTO.getCategoryId());
        Product product = productRepository.findById(requestDTO.getProductId()).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.setCategory(category);
        return createProductDTOWithoutStore(productRepository.save(product));
    }

    public SliceImpl<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryService.getCategoryById(categoryId);
        Slice<Product> productSlice = productRepository.findByCategory(category, pageable);

        if (productSlice.hasContent()) {
            List<ProductDTO> productDTOS = productSlice.getContent().stream().map(product -> createProductDTO(product, productStoreRepository.findTopByProductIdOrderByPriceAsc(product.getId()))).toList();
            return new SliceImpl<>(productDTOS, productSlice.getPageable(), productSlice.hasNext());
        }

        return new SliceImpl<>(new ArrayList<>(), pageable, false);
    }

    private ProductStore getOrCreateProductStore(ProductsDTO productsDTO, ItemDTO item, Product product) {
        return productStoreRepository.findByProductIdAndBranchId(product.getId(), productsDTO.getBranchId())
                .orElseGet(() -> createAndSaveProductStore(productsDTO.getBranchId(), product, item.getUnitValue(), item.getUnit()));
    }

    private ProductStore createAndSaveProductStore(UUID branchId, Product product, BigDecimal price, String unit) {
        ProductStore productStore = new ProductStore();
        productStore.setProduct(product);
        productStore.setBranchId(branchId);
        productStore.setPrice(price);
        productStore.setUnit(unit);
        productStore.setCreatedAt(LocalDateTime.now());
        return productStoreRepository.save(productStore);
    }

    private void updatePriceIfNecessary(ProductStore productStore, BigDecimal price) {
        if (!productStore.getPrice().equals(price)) {
            saveProductHistory(productStore);
            productStore.setPrice(price);
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
        return getProductByCode(itemDTO.getCode())
                .map(existingProduct -> updateProduct(existingProduct, itemDTO))
                .orElseGet(() -> saveProduct(itemDTO));
    }

    private Optional<Product> getProductByCode(String code) {
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
        productDTO.setUnit(productStore.getUnit());
        productDTO.setCategory(nonNull(product.getCategory()) ? product.getCategory().getName() : null);
        productDTO.setStoreId(productStore.getBranchId());
        productDTO.setImage(product.getUrlImage());
        return productDTO;
    }

    private ProductDTO createProductDTOWithoutStore(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setCode(product.getCode());
        productDTO.setCategory(nonNull(product.getCategory()) ? product.getCategory().getName() : null);
        productDTO.setImage(product.getUrlImage());
        return productDTO;
    }

}
