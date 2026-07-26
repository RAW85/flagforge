package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.audit.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataAuditEventJpaRepository extends JpaRepository<AuditEvent, UUID> {
}
