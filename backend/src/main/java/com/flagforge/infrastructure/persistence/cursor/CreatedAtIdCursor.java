package com.flagforge.infrastructure.persistence.cursor;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Opaque cursor: base64url("epochMillis:uuid").
 */
public record CreatedAtIdCursor(Instant createdAt, UUID id) {

	private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

	public String encode() {
		String raw = createdAt.toEpochMilli() + ":" + id;
		return ENCODER.encodeToString(raw.getBytes(StandardCharsets.UTF_8));
	}

	public static CreatedAtIdCursor decode(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return null;
		}
		try {
			String raw = new String(DECODER.decode(cursor), StandardCharsets.UTF_8);
			int sep = raw.indexOf(':');
			if (sep <= 0) {
				throw new IllegalArgumentException("Invalid cursor format");
			}
			Instant createdAt = Instant.ofEpochMilli(Long.parseLong(raw.substring(0, sep)));
			UUID id = UUID.fromString(raw.substring(sep + 1));
			return new CreatedAtIdCursor(createdAt, id);
		} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
			throw new IllegalArgumentException("Invalid cursor token", ex);
		}
	}
}
