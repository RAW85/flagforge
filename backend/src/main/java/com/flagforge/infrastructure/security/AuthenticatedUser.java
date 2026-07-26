package com.flagforge.infrastructure.security;

import com.flagforge.domain.user.User;
import com.flagforge.domain.user.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Security principal derived from JWT or the User aggregate.
 */
public record AuthenticatedUser(
		UUID id,
		String username,
		String email,
		UserRole role,
		boolean enabled
) implements UserDetails {

	public static AuthenticatedUser from(User user) {
		return new AuthenticatedUser(
				user.getId(),
				user.getUsername(),
				user.getEmail(),
				user.getRole(),
				user.isEnabled()
		);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
