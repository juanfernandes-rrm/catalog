package br.ufpr.tads.catalog.catalog.domain.repository;

import br.ufpr.tads.catalog.catalog.domain.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
