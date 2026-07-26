package com.flagforge.domain.audit;

/**
 * Actions recorded in the audit trail.
 */
public enum AuditAction {
	CREATE,
	UPDATE,
	DELETE,
	ENABLE,
	DISABLE,
	ARCHIVE,
	ROLLBACK,
	EVALUATE,
	LOGIN,
	LOGOUT
}
