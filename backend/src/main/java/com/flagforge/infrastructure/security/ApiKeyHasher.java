package com.flagforge.infrastructure.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Generates FlagForge SDK keys and stores only SHA-256 hashes.
 * <p>
 * Format: {@code ffk_<8-char-prefix>_<32-char-secret>}
 */
@Component
public class ApiKeyHasher {

	private static final String PREFIX = "ffk_";
	private static final SecureRandom RANDOM = new SecureRandom();

	public GeneratedApiKey generate() {
		String keyPrefix = randomHex(4); // 8 hex chars
		String secret = randomHex(16);   // 32 hex chars
		String rawKey = PREFIX + keyPrefix + "_" + secret;
		return new GeneratedApiKey(rawKey, keyPrefix, sha256Hex(rawKey));
	}

	public String hash(String rawKey) {
		return sha256Hex(rawKey);
	}

	public boolean looksLikeApiKey(String value) {
		return value != null && value.startsWith(PREFIX) && value.length() > 20;
	}

	private String randomHex(int byteCount) {
		byte[] bytes = new byte[byteCount];
		RANDOM.nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}

	private String sha256Hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 not available", ex);
		}
	}

	public record GeneratedApiKey(String rawKey, String keyPrefix, String keyHash) {
	}
}
