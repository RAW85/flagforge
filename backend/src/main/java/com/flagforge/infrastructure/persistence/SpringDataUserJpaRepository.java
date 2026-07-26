package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserJpaRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	List<User> findAllByOrderByCreatedAtDesc();

	long countByRoleAndEnabledTrue(UserRole role);
}
