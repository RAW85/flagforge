package com.flagforge.infrastructure.security;

import com.flagforge.domain.flag.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Security principal for SDK API keys.
 */
public record ApiKeyPrincipal(
		UUID keyId,
		String name,
		String keyPrefix,
		UUID ownerId,
		Environment environmentScope
) implements UserDetails {

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_SDK"));
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return "apikey:" + keyPrefix;
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
		return true;
	}
}
