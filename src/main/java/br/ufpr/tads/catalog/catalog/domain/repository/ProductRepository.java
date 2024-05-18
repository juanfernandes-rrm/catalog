package br.ufpr.tads.catalog.catalog.domain.repository;

import br.ufpr.tads.catalog.catalog.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>{

    Optional<Product> findByCode(String code);

}
