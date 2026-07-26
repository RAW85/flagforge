package com.flagforge.domain.common;

/**
 * Cursor-based pagination request (shared domain primitive).
 * <p>
 * Clients pass an opaque {@code cursor} from a previous page; {@code null}/{@code blank}
 * means "start from the beginning".
 *
 * @param cursor opaque cursor token (typically last seen id / composite key)
 * @param limit  page size (capped by application layer)
 */
public record CursorPageRequest(String cursor, int limit) {

	public static final int DEFAULT_LIMIT = 20;
	public static final int MAX_LIMIT = 100;

	public CursorPageRequest {
		if (limit <= 0) {
			limit = DEFAULT_LIMIT;
		}
		if (limit > MAX_LIMIT) {
			limit = MAX_LIMIT;
		}
	}

	public static CursorPageRequest of(String cursor, Integer limit) {
		return new CursorPageRequest(cursor, limit == null ? DEFAULT_LIMIT : limit);
	}
}
