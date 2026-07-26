package com.flagforge.domain.common;

import java.util.List;

/**
 * Cursor-based pagination response.
 *
 * @param items      page of results
 * @param nextCursor cursor for the next page, or {@code null} if no more data
 * @param hasMore    whether another page exists
 * @param <T>        item type
 */
public record CursorPage<T>(List<T> items, String nextCursor, boolean hasMore) {

	public static <T> CursorPage<T> of(List<T> items, String nextCursor, boolean hasMore) {
		return new CursorPage<>(List.copyOf(items), nextCursor, hasMore);
	}

	public static <T> CursorPage<T> empty() {
		return new CursorPage<>(List.of(), null, false);
	}
}
