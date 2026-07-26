package com.flagforge.domain.repository;

import com.flagforge.domain.apikey.ApiKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository {

	ApiKey save(ApiKey apiKey);

	Optional<ApiKey> findById(UUID id);

	Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash);

	List<ApiKey> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

	List<ApiKey> findAllByOrderByCreatedAtDesc();
}
