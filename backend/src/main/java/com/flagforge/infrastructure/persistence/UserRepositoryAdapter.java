package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.repository.UserRepository;
import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

	private final SpringDataUserJpaRepository jpa;

	@Override
	public User save(User user) {
		return jpa.save(user);
	}

	@Override
	public Optional<User> findById(UUID id) {
		return jpa.findById(id);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return jpa.findByEmail(email);
	}

	@Override
	public Optional<User> findByUsername(String username) {
		return jpa.findByUsername(username);
	}

	@Override
	public boolean existsByEmail(String email) {
		return jpa.existsByEmail(email);
	}

	@Override
	public boolean existsByUsername(String username) {
		return jpa.existsByUsername(username);
	}

	@Override
	public long count() {
		return jpa.count();
	}

	@Override
	public List<User> findAllByOrderByCreatedAtDesc() {
		return jpa.findAllByOrderByCreatedAtDesc();
	}

	@Override
	public long countByRoleAndEnabledTrue(UserRole role) {
		return jpa.countByRoleAndEnabledTrue(role);
	}
}
