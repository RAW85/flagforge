package com.flagforge.application.command.flag;

import com.flagforge.domain.audit.AuditAction;
import com.flagforge.domain.audit.AuditEvent;
import com.flagforge.domain.evaluation.FlagSnapshot;
import com.flagforge.domain.event.FeatureFlagDomainEvent;
import com.flagforge.domain.exception.BusinessRuleException;
import com.flagforge.domain.exception.ConflictException;
import com.flagforge.domain.exception.ResourceNotFoundException;
import com.flagforge.domain.flag.FeatureFlag;
import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.domain.flag.FlagType;
import com.flagforge.domain.repository.AuditEventRepository;
import com.flagforge.domain.repository.DomainEventPublisher;
import com.flagforge.domain.repository.FeatureFlagRepository;
import com.flagforge.domain.repository.FlagCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Write-side handler for feature flags: persist, refresh {@link FlagCache}, audit, publish events.
 */
@Service
@RequiredArgsConstructor
public class FeatureFlagCommandHandler {

	private final FeatureFlagRepository featureFlagRepository;
	private final AuditEventRepository auditEventRepository;
	private final FlagCache flagCache;
	private final DomainEventPublisher eventPublisher;

	@Transactional
	public FeatureFlag handle(CreateFeatureFlagCommand command) {
		validatePercentage(command.flagType(), command.percentage());

		if (featureFlagRepository.existsByKeyAndEnvironment(command.key(), command.environment())) {
			throw new ConflictException(
					"Feature flag already exists for key '%s' in environment %s"
							.formatted(command.key(), command.environment())
			);
		}

		FeatureFlag flag = FeatureFlag.builder()
				.key(command.key().trim())
				.name(command.name().trim())
				.description(command.description())
				.enabled(false)
				.status(FlagStatus.DRAFT)
				.flagType(command.flagType() != null ? command.flagType() : FlagType.BOOLEAN)
				.environment(command.environment())
				.defaultValue(command.defaultValue() != null ? command.defaultValue() : "false")
				.percentage(command.percentage())
				.rulesJson(command.rulesJson())
				.createdBy(command.createdBy())
				.build();

		FeatureFlag saved = featureFlagRepository.save(flag);
		refreshCache(saved);
		writeAudit(saved, AuditAction.CREATE, command.createdBy(), null);
		publishFlagEvent(FeatureFlagDomainEvent.CREATED, saved, command.createdBy());
		return saved;
	}

	@Transactional
	public FeatureFlag handle(UpdateFeatureFlagCommand command) {
		FeatureFlag flag = requireFlag(command.id());
		validatePercentage(
				command.flagType() != null ? command.flagType() : flag.getFlagType(),
				command.percentage() != null ? command.percentage() : flag.getPercentage()
		);

		if (command.name() != null && !command.name().isBlank()) {
			flag.setName(command.name().trim());
		}
		if (command.description() != null) {
			flag.setDescription(command.description());
		}
		if (command.flagType() != null) {
			flag.setFlagType(command.flagType());
		}
		if (command.status() != null) {
			flag.setStatus(command.status());
		}
		if (command.defaultValue() != null) {
			flag.setDefaultValue(command.defaultValue());
		}
		if (command.percentage() != null) {
			flag.setPercentage(command.percentage());
		}
		if (command.rulesJson() != null) {
			flag.setRulesJson(command.rulesJson());
		}

		FeatureFlag saved = featureFlagRepository.save(flag);
		refreshCache(saved);
		writeAudit(saved, AuditAction.UPDATE, command.actorId(), null);
		publishFlagEvent(FeatureFlagDomainEvent.UPDATED, saved, command.actorId());
		return saved;
	}

	@Transactional
	public FeatureFlag handle(ToggleFeatureFlagCommand command) {
		FeatureFlag flag = requireFlag(command.id());

		if (command.enabled() && flag.getStatus() == FlagStatus.ARCHIVED) {
			throw new BusinessRuleException("Cannot enable an archived feature flag");
		}

		flag.setEnabled(command.enabled());
		if (command.enabled() && flag.getStatus() == FlagStatus.DRAFT) {
			flag.setStatus(FlagStatus.ACTIVE);
		}

		FeatureFlag saved = featureFlagRepository.save(flag);
		refreshCache(saved);
		writeAudit(
				saved,
				command.enabled() ? AuditAction.ENABLE : AuditAction.DISABLE,
				command.actorId(),
				null
		);
		publishFlagEvent(FeatureFlagDomainEvent.TOGGLED, saved, command.actorId());
		return saved;
	}

	@Transactional
	public void handle(DeleteFeatureFlagCommand command) {
		FeatureFlag flag = requireFlag(command.id());
		featureFlagRepository.delete(flag);
		flagCache.evict(flag.getKey(), flag.getEnvironment());
		writeAudit(flag, AuditAction.DELETE, command.actorId(), null);
		publishFlagEvent(FeatureFlagDomainEvent.DELETED, flag, command.actorId());
	}

	private void publishFlagEvent(String type, FeatureFlag flag, java.util.UUID actorId) {
		eventPublisher.publish(FeatureFlagDomainEvent.of(
				type,
				flag.getId(),
				flag.getKey(),
				flag.getEnvironment(),
				flag.isEnabled(),
				flag.getStatus(),
				flag.getFlagType(),
				flag.getPercentage(),
				actorId
		));
	}

	private void refreshCache(FeatureFlag flag) {
		flagCache.put(FlagSnapshot.from(flag));
	}

	private FeatureFlag requireFlag(java.util.UUID id) {
		return featureFlagRepository.findById(id)
				.orElseThrow(() -> ResourceNotFoundException.of("FeatureFlag", id));
	}

	private void validatePercentage(FlagType type, Integer percentage) {
		if (type == FlagType.PERCENTAGE) {
			if (percentage == null || percentage < 0 || percentage > 100) {
				throw new BusinessRuleException(
						"PERCENTAGE flags require percentage between 0 and 100"
				);
			}
		}
	}

	private void writeAudit(FeatureFlag flag, AuditAction action, java.util.UUID actorId, String details) {
		auditEventRepository.save(AuditEvent.builder()
				.entityType("FeatureFlag")
				.entityId(flag.getId())
				.action(action)
				.actorId(actorId)
				.detailsJson(details)
				.build());
	}
}
