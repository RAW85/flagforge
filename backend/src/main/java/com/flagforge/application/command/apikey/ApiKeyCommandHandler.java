package com.flagforge.application.command.apikey;

import com.flagforge.domain.apikey.ApiKey;
import com.flagforge.domain.audit.AuditAction;
import com.flagforge.domain.audit.AuditEvent;
import com.flagforge.domain.exception.BusinessRuleException;
import com.flagforge.domain.exception.ResourceNotFoundException;
import com.flagforge.domain.repository.ApiKeyRepository;
import com.flagforge.domain.repository.AuditEventRepository;
import com.flagforge.infrastructure.security.ApiKeyHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Creates and revokes SDK API keys. Raw secret is returned only from create.
 */
@Service
@RequiredArgsConstructor
public class ApiKeyCommandHandler {

	private final ApiKeyRepository apiKeyRepository;
	private final AuditEventRepository auditEventRepository;
	private final ApiKeyHasher apiKeyHasher;

	/** Generates key material, stores only the hash; result includes the one-time raw key. */
	@Transactional
	public CreateApiKeyResult handle(CreateApiKeyCommand command) {
		if (command.name() == null || command.name().isBlank()) {
			throw new BusinessRuleException("API key name is required");
		}

		ApiKeyHasher.GeneratedApiKey generated = apiKeyHasher.generate();

		ApiKey apiKey = ApiKey.builder()
				.name(command.name().trim())
				.keyPrefix(generated.keyPrefix())
				.keyHash(generated.keyHash())
				.ownerId(command.ownerId())
				.environmentScope(command.environmentScope())
				.active(true)
				.build();

		ApiKey saved = apiKeyRepository.save(apiKey);

		auditEventRepository.save(AuditEvent.builder()
				.entityType("ApiKey")
				.entityId(saved.getId())
				.action(AuditAction.CREATE)
				.actorId(command.ownerId())
				.detailsJson("{\"prefix\":\"" + saved.getKeyPrefix() + "\"}")
				.build());

		return new CreateApiKeyResult(saved, generated.rawKey());
	}

	@Transactional
	public ApiKey handle(RevokeApiKeyCommand command) {
		ApiKey apiKey = apiKeyRepository.findById(command.id())
				.orElseThrow(() -> ResourceNotFoundException.of("ApiKey", command.id()));

		if (!apiKey.isActive()) {
			throw new BusinessRuleException("API key is already revoked");
		}

		apiKey.setActive(false);
		apiKey.setRevokedAt(Instant.now());
		ApiKey saved = apiKeyRepository.save(apiKey);

		auditEventRepository.save(AuditEvent.builder()
				.entityType("ApiKey")
				.entityId(saved.getId())
				.action(AuditAction.DELETE)
				.actorId(command.actorId())
				.detailsJson("{\"event\":\"REVOKE\",\"prefix\":\"" + saved.getKeyPrefix() + "\"}")
				.build());

		return saved;
	}
}
