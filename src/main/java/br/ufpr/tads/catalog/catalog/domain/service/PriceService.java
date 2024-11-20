package br.ufpr.tads.catalog.catalog.domain.service;


import br.ufpr.tads.catalog.catalog.domain.client.RegisterClient;
import br.ufpr.tads.catalog.catalog.domain.model.PriceHistory;
import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.repository.PriceHistoryRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductStoreRepository;
import br.ufpr.tads.catalog.catalog.domain.response.PriceHistoryResponseDTO;
import br.ufpr.tads.catalog.catalog.domain.response.ProductsPriceResponseDTO;
import br.ufpr.tads.catalog.catalog.dto.commons.BranchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
public class PriceService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductStoreRepository productStoreRepository;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private RegisterClient registerClient;

    public SliceImpl<PriceHistoryResponseDTO> getPriceHistory(UUID productId, Pageable pageable) {
        Slice<PriceHistory> priceHistorySlice = priceHistoryRepository.findByProductId(productId, pageable);

        List<PriceHistoryResponseDTO> responses = new ArrayList<>();

        if (priceHistorySlice.hasContent()) {
            responses = priceHistorySlice.getContent().stream()
                    .map(this::createPriceHistoryResponse)
                    .collect(Collectors.toList());
        } else {
            ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(productId);
            PriceHistoryResponseDTO responseDTO = createPriceHistoryResponseFromProductStore(productStore);
            responses.add(responseDTO);
        }

        return new SliceImpl<>(responses, pageable, priceHistorySlice.hasNext());
    }


    public SliceImpl<ProductsPriceResponseDTO> calculateTotalPriceByStore(
            br.ufpr.tads.catalog.catalog.domain.request.ProductsPriceRequestDTO productsPriceRequestDTO, Pageable pageable) {

        List<BranchDTO> nearbyBranches = registerClient.getNearbyBranches(
                productsPriceRequestDTO.getCep(), productsPriceRequestDTO.getDistance());
        List<UUID> nearbyBranchIds = nearbyBranches.stream()
                .map(BranchDTO::getCorrelationId)
                .toList();

        Page<Product> products = productRepository.findAllByIdIn(
                productsPriceRequestDTO.getProductIdList(), pageable);

        List<ProductStore> filteredProductStores = productStoreRepository.findByProductIdInAndBranchIdIn(
                products.getContent().stream().map(Product::getId).toList(),
                nearbyBranchIds);

        Map<UUID, ProductsPriceResponseDTO> storeResponses = new HashMap<>();

        filteredProductStores.forEach(productStore -> {
            UUID storeId = productStore.getBranchId();

            ProductsPriceResponseDTO response = storeResponses.computeIfAbsent(storeId, id -> {
                BranchDTO branchDTO = nearbyBranches.stream()
                        .filter(branch -> branch.getCorrelationId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Loja não encontrada!"));

                return ProductsPriceResponseDTO.builder()
                        .branch(branchDTO)
                        .totalPrice(BigDecimal.ZERO)
                        .productQuantity(0)
                        .productsId(new ArrayList<>())
                        .build();
            });

            response.setTotalPrice(response.getTotalPrice().add(productStore.getPrice()));
            response.setProductQuantity(response.getProductQuantity() + 1);
            response.getProductsId().add(productStore.getProduct().getId());
        });

        return new SliceImpl<>(new ArrayList<>(storeResponses.values()), pageable, products.hasNext());
    }

    private PriceHistoryResponseDTO createPriceHistoryResponse(PriceHistory priceHistory) {
        ProductStore productStore = productStoreRepository.findTopByProductIdOrderByPriceAsc(priceHistory.getProductStore().getProduct().getId());
        PriceHistoryResponseDTO responseDTO = new PriceHistoryResponseDTO();
        responseDTO.setStoreId(productStore.getBranchId());

        BranchDTO branch = registerClient.getBranch(productStore.getBranchId());
        responseDTO.setStoreName(nonNull(branch) ? branch.getStore().getName() : "Loja não encontrada");
        responseDTO.setBranchId(branch.getId());
        responseDTO.setPrice(priceHistory.getPrice());
        responseDTO.setPriceChangeDate(priceHistory.getCreatedAt());

        return responseDTO;
    }

    private PriceHistoryResponseDTO createPriceHistoryResponseFromProductStore(ProductStore productStore) {
        PriceHistoryResponseDTO responseDTO = new PriceHistoryResponseDTO();
        responseDTO.setStoreId(productStore.getBranchId());

        BranchDTO branch = registerClient.getBranch(productStore.getBranchId());
        responseDTO.setStoreName(nonNull(branch) ? branch.getStore().getName() : "Loja não encontrada");
        responseDTO.setBranchId(branch.getId());
        responseDTO.setPrice(productStore.getPrice());
        responseDTO.setPriceChangeDate(null);

        return responseDTO;
    }

}
