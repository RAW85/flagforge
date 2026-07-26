package com.flagforge.domain.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Append-only audit trail for security and compliance.
 */
@Entity
@Table(
		name = "audit_events",
		indexes = {
				@Index(name = "idx_audit_events_entity", columnList = "entityType, entityId"),
				@Index(name = "idx_audit_events_actor", columnList = "actorId"),
				@Index(name = "idx_audit_events_occurred_at", columnList = "occurredAt")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	/**
	 * Aggregate type (e.g. "FeatureFlag", "User").
	 */
	@Column(nullable = false, length = 64)
	private String entityType;

	@Column
	private UUID entityId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AuditAction action;

	@Column
	private UUID actorId;

	@Column(length = 255)
	private String actorEmail;

	/**
	 * Optional JSON diff / payload describing the change.
	 */
	@Column(columnDefinition = "TEXT")
	private String detailsJson;

	@Column(length = 64)
	private String ipAddress;

	@Column(nullable = false, updatable = false)
	private Instant occurredAt;

	@PrePersist
	protected void onCreate() {
		if (this.occurredAt == null) {
			this.occurredAt = Instant.now();
		}
	}
}
