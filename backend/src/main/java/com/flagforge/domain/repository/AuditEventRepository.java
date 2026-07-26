package com.flagforge.domain.repository;

import com.flagforge.domain.audit.AuditEvent;

public interface AuditEventRepository {

	AuditEvent save(AuditEvent event);
}
