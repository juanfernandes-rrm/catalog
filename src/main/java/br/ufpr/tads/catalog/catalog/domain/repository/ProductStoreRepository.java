package br.ufpr.tads.catalog.catalog.domain.repository;

import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductStoreRepository extends JpaRepository<ProductStore, UUID> {

    ProductStore findFirstByProductIdOrderByPriceAsc(UUID productId);

    Optional<ProductStore> findByProductIdAndBranchId(UUID id, UUID branchId);

    @Query("SELECT ps FROM ProductStore ps WHERE ps.product.id IN :productIds AND ps.branchId IN :branchIds")
    List<ProductStore> findByProductIdInAndBranchIdIn(
            List<UUID> productIds, List<UUID> branchIds);
}
