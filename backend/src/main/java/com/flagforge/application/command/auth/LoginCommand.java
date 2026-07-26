package com.flagforge.application.command.auth;

public record LoginCommand(
		String email,
		String password
) {
}
