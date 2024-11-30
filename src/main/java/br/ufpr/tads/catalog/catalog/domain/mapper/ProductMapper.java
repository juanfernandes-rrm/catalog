package br.ufpr.tads.catalog.catalog.domain.mapper;

import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.response.commons.ProductDTO;
import org.springframework.stereotype.Component;

import static java.util.Objects.nonNull;

@Component
public class ProductMapper {

    public ProductDTO createProductDTO(Product product, ProductStore productStore) {
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

    public ProductDTO createProductDTOWithoutStore(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setCode(product.getCode());
        productDTO.setCategory(nonNull(product.getCategory()) ? product.getCategory().getName() : null);
        productDTO.setImage(product.getUrlImage());
        return productDTO;
    }

}
