package com.flagforge.domain.apikey;

import com.flagforge.domain.common.BaseEntity;
import com.flagforge.domain.flag.Environment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Server-side SDK credential. The raw secret is shown only once at creation;
 * only a SHA-256 hash is stored.
 */
@Entity
@Table(
		name = "api_keys",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_api_keys_key_hash", columnNames = "keyHash"),
				@UniqueConstraint(name = "uk_api_keys_key_prefix", columnNames = "keyPrefix")
		},
		indexes = {
				@Index(name = "idx_api_keys_owner", columnList = "ownerId"),
				@Index(name = "idx_api_keys_active", columnList = "active")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 100)
	private String name;

	/**
	 * Public identifier fragment used for display (e.g. {@code a1b2c3d4}).
	 */
	@Column(nullable = false, length = 16)
	private String keyPrefix;

	/**
	 * SHA-256 hex hash of the full API key string.
	 */
	@Column(nullable = false, length = 64)
	private String keyHash;

	@Column(nullable = false)
	private UUID ownerId;

	/**
	 * Optional environment scope. {@code null} means all environments.
	 */
	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private Environment environmentScope;

	@Column(nullable = false)
	@Builder.Default
	private boolean active = true;

	@Column
	private Instant lastUsedAt;

	@Column
	private Instant revokedAt;
}
