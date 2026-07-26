package com.flagforge.presentation.dto.common;

import com.flagforge.domain.common.CursorPage;

import java.util.List;
import java.util.function.Function;

public record CursorPageResponse<T>(
		List<T> items,
		String nextCursor,
		boolean hasMore
) {

	public static <S, T> CursorPageResponse<T> from(CursorPage<S> page, Function<S, T> mapper) {
		List<T> mapped = page.items().stream().map(mapper).toList();
		return new CursorPageResponse<>(mapped, page.nextCursor(), page.hasMore());
	}
}
