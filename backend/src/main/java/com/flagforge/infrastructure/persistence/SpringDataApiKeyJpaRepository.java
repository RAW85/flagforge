package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.apikey.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataApiKeyJpaRepository extends JpaRepository<ApiKey, UUID> {

	Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);

	List<ApiKey> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

	List<ApiKey> findAllByOrderByCreatedAtDesc();
}
