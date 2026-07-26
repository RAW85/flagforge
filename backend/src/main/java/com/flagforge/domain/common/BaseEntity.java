package com.flagforge.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Shared audit and optimistic-locking fields for all domain aggregates.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	@Version
	@Column(nullable = false)
	private Long version;

	@PrePersist
	protected void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
		if (this.version == null) {
			this.version = 0L;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = Instant.now();
	}
}
