package com.flagforge.domain.repository;

import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

	User save(User user);

	Optional<User> findById(UUID id);

	Optional<User> findByEmail(String email);

	Optional<User> findByUsername(String username);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);

	long count();

	List<User> findAllByOrderByCreatedAtDesc();

	long countByRoleAndEnabledTrue(UserRole role);
}
