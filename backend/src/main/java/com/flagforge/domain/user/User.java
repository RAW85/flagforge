package com.flagforge.domain.user;

import com.flagforge.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Platform user who can manage or evaluate feature flags.
 */
@Entity
@Table(
		name = "users",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_users_email", columnNames = "email"),
				@UniqueConstraint(name = "uk_users_username", columnNames = "username")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 100)
	private String username;

	@Column(nullable = false, length = 255)
	private String email;

	@Column(nullable = false, length = 255)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	@Builder.Default
	private UserRole role = UserRole.VIEWER;

	@Column(nullable = false)
	@Builder.Default
	private boolean enabled = true;
}
