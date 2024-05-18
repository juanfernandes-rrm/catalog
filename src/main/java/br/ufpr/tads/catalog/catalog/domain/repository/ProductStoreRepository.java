package br.ufpr.tads.catalog.catalog.domain.repository;

import br.ufpr.tads.catalog.catalog.domain.model.ProductStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductStoreRepository extends JpaRepository<ProductStore, UUID> {
}
