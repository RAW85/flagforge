package com.flagforge.application.command.auth;

public record RegisterUserCommand(
		String username,
		String email,
		String password
) {
}
