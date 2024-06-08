package br.ufpr.tads.catalog.catalog.domain.service;

import br.ufpr.tads.catalog.catalog.domain.model.Address;
import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.model.Store;
import br.ufpr.tads.catalog.catalog.domain.repository.AddressRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductStoreRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.StoreRepository;
import br.ufpr.tads.catalog.catalog.domain.response.AddressDTO;
import br.ufpr.tads.catalog.catalog.domain.response.ItemDTO;
import br.ufpr.tads.catalog.catalog.domain.response.ReceiptResponseDTO;
import br.ufpr.tads.catalog.catalog.domain.response.StoreDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.ProductResponseDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private AddressRepository addressRepository;


    public void processReceipt(ReceiptResponseDTO response) {
        processItems(response.getItems(), processStore(response.getStore()));
    }

    public Page<ProductResponseDTO> getProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapProduct);
    }

    public ProductResponseDTO searchProductsByName(String name) {
        String treatedName = StringUtils.stripAccents(name).trim();
        return productRepository.findByNameContainingIgnoreCase(treatedName).stream()
                .map(this::mapProduct)
                .findFirst()
                .orElse(null);
    }

    private ProductResponseDTO mapProduct(Product product) {
        return new ProductResponseDTO(product.getId(), product.getName(), product.getCode());
    }

    private StoreDTO mapStore(Store store) {
        StoreDTO storeDTO = new StoreDTO();
        storeDTO.setCNPJ(store.getCNPJ());
        storeDTO.setName(store.getName());
        storeDTO.setAddress(mapAddress(store.getAddress()));
        return storeDTO;
    }

    private AddressDTO mapAddress(Address address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setCity(address.getCity());
        addressDTO.setState(address.getState());
        addressDTO.setNeighborhood(address.getNeighborhood());
        addressDTO.setStreet(address.getStreet());
        addressDTO.setNumber(address.getNumber());
        return addressDTO;
    }

    private Store processStore(StoreDTO store) {
        return getStore(store.getCNPJ()).orElseGet(() -> {
            Store storeEntity = new Store();
            storeEntity.setCNPJ(store.getCNPJ());
            storeEntity.setName(store.getName());
            storeEntity.setAddress(processAddress(store.getAddress()));
            return storeRepository.save(storeEntity);
        });
    }

    private Address processAddress(AddressDTO address) {
        Address addressEntity = new Address();
        addressEntity.setCity(address.getCity());
        addressEntity.setNumber(address.getNumber());
        addressEntity.setState(address.getState());
        addressEntity.setStreet(address.getStreet());
        addressEntity.setNeighborhood(address.getNeighborhood());
        return addressRepository.save(addressEntity);
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
