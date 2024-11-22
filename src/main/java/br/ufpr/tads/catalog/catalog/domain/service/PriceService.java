package br.ufpr.tads.catalog.catalog.domain.service;


import br.ufpr.tads.catalog.catalog.domain.client.RegisterClient;
import br.ufpr.tads.catalog.catalog.domain.model.PriceHistory;
import br.ufpr.tads.catalog.catalog.domain.model.Product;
import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import br.ufpr.tads.catalog.catalog.domain.repository.PriceHistoryRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductRepository;
import br.ufpr.tads.catalog.catalog.domain.repository.ProductStoreRepository;
import br.ufpr.tads.catalog.catalog.domain.request.ProductItemRequestDTO;
import br.ufpr.tads.catalog.catalog.domain.request.ProductsPriceRequestDTO;
import br.ufpr.tads.catalog.catalog.domain.response.PriceHistoryResponseDTO;
import br.ufpr.tads.catalog.catalog.domain.response.ProductItemResponseDTO;
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


    public SliceImpl<ProductsPriceResponseDTO> calculateTotalPriceByStore(ProductsPriceRequestDTO productsPriceRequestDTO, Pageable pageable) {
        List<BranchDTO> nearbyBranches = registerClient.getNearbyBranches(
                productsPriceRequestDTO.getCep(), productsPriceRequestDTO.getDistance());
        List<UUID> nearbyBranchIds = nearbyBranches.stream()
                .map(BranchDTO::getCorrelationId)
                .toList();

        Map<UUID, Integer> productQuantities = productsPriceRequestDTO.getProducts().stream()
                .collect(Collectors.toMap(ProductItemRequestDTO::getProductId, ProductItemRequestDTO::getQuantity));

        List<UUID> productIdList = new ArrayList<>(productQuantities.keySet());
        Page<Product> products = productRepository.findAllByIdIn(productIdList, pageable);

        List<ProductStore> filteredProductStores = productStoreRepository.findByProductIdInAndBranchIdIn(
                products.getContent().stream().map(Product::getId).toList(),
                nearbyBranchIds);

        Map<UUID, ProductsPriceResponseDTO> storeResponses = new HashMap<>();

        filteredProductStores.forEach(productStore -> {
            UUID branchId = productStore.getBranchId();

            ProductsPriceResponseDTO response = storeResponses.computeIfAbsent(branchId, id -> {
                BranchDTO branchDTO = nearbyBranches.stream()
                        .filter(branch -> branch.getCorrelationId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Loja não encontrada!"));

                return ProductsPriceResponseDTO.builder()
                        .branch(branchDTO)
                        .totalPrice(BigDecimal.ZERO)
                        .productQuantity(0)
                        .products(new ArrayList<>())
                        .build();
            });

            int quantity = productQuantities.getOrDefault(productStore.getProduct().getId(), 1);
            BigDecimal totalPrice = productStore.getPrice().multiply(BigDecimal.valueOf(quantity));

            ProductItemResponseDTO productResponse = ProductItemResponseDTO.builder()
                    .productId(productStore.getProduct().getId())
                    .quantity(quantity)
                    .price(productStore.getPrice())
                    .total(totalPrice)
                    .build();

            response.getProducts().add(productResponse);
            response.setTotalPrice(response.getTotalPrice().add(totalPrice));
            response.setProductQuantity(response.getProductQuantity() + quantity);
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
