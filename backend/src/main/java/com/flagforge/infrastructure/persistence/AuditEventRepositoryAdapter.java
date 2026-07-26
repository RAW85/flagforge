package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.audit.AuditEvent;
import com.flagforge.domain.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AuditEventRepositoryAdapter implements AuditEventRepository {

	private final SpringDataAuditEventJpaRepository jpa;

	@Override
	public AuditEvent save(AuditEvent event) {
		return jpa.save(event);
	}
}
