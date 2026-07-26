package com.flagforge.application.query.apikey;

import com.flagforge.domain.apikey.ApiKey;
import com.flagforge.domain.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Lists API keys (all, or filtered by owner). Never returns raw secrets. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiKeyQueryHandler {

	private final ApiKeyRepository apiKeyRepository;

	public List<ApiKey> handle(ListApiKeysQuery query) {
		if (query.ownerId() != null) {
			return apiKeyRepository.findByOwnerIdOrderByCreatedAtDesc(query.ownerId());
		}
		return apiKeyRepository.findAllByOrderByCreatedAtDesc();
	}
}
