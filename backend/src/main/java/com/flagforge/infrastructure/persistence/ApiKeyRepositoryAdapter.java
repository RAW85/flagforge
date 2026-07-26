package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.apikey.ApiKey;
import com.flagforge.domain.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ApiKeyRepositoryAdapter implements ApiKeyRepository {

	private final SpringDataApiKeyJpaRepository jpa;

	@Override
	public ApiKey save(ApiKey apiKey) {
		return jpa.save(apiKey);
	}

	@Override
	public Optional<ApiKey> findById(UUID id) {
		return jpa.findById(id);
	}

	@Override
	public Optional<ApiKey> findByKeyHashAndActiveTrue(String keyHash) {
		return jpa.findByKeyHashAndActiveTrue(keyHash);
	}

	@Override
	public List<ApiKey> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId) {
		return jpa.findByOwnerIdOrderByCreatedAtDesc(ownerId);
	}

	@Override
	public List<ApiKey> findAllByOrderByCreatedAtDesc() {
		return jpa.findAllByOrderByCreatedAtDesc();
	}
}
