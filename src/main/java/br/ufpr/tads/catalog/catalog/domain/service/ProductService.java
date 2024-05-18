package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.model.Store;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductStoreRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.StoreRepository;
import br.ufpr.tads.catalog.catalog.domain.response.ItemDTO;
import br.ufpr.tads.catalog.catalog.domain.response.ReceiptResponseDTO;
import br.ufpr.tads.catalog.catalog.domain.response.StoreDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductStoreRepository productStoreRepository;


    public void processReceipt(ReceiptResponseDTO response) {
        processItems(response.getItems(), processStore(response.getStore()));
    }

    private Store processStore(StoreDTO store) {
        return getStore(store.getCNPJ()).orElseGet(() -> {
            Store storeEntity = new Store();
            storeEntity.setCNPJ(store.getCNPJ());
            storeEntity.setName(store.getName());
            return storeRepository.save(storeEntity);
        });
    }

    private Optional<Store> getStore(String cnpj) {
        return storeRepository.findByCNPJ(cnpj);
    }

    private void processItems(List<ItemDTO> items, Store store) {
        productStoreRepository.saveAll(items.stream()
                .map(item -> {
                    Product product = saveOrUpdateProduct(item);
                    ProductStore productStore = new ProductStore();
                    productStore.setProduct(product);
                    productStore.setStore(store);
                    productStore.setPrice(item.getUnitValue());
                    productStore.setUnit(item.getUnit());
                    return productStore;
                })
                .toList());
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
