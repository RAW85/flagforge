package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.common.CursorPage;
import com.flagforge.domain.common.CursorPageRequest;
import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FeatureFlag;
import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.domain.repository.FeatureFlagRepository;
import com.flagforge.infrastructure.persistence.cursor.CreatedAtIdCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FeatureFlagRepositoryAdapter implements FeatureFlagRepository {

	private final SpringDataFeatureFlagJpaRepository jpa;

	@Override
	public FeatureFlag save(FeatureFlag flag) {
		return jpa.save(flag);
	}

	@Override
	public Optional<FeatureFlag> findById(UUID id) {
		return jpa.findById(id);
	}

	@Override
	public Optional<FeatureFlag> findByKeyAndEnvironment(String key, Environment environment) {
		return jpa.findByKeyAndEnvironment(key, environment);
	}

	@Override
	public boolean existsByKeyAndEnvironment(String key, Environment environment) {
		return jpa.existsByKeyAndEnvironment(key, environment);
	}

	@Override
	public void delete(FeatureFlag flag) {
		jpa.delete(flag);
	}

	@Override
	public CursorPage<FeatureFlag> findPage(
			CursorPageRequest pageRequest,
			Environment environment,
			FlagStatus status
	) {
		int fetchSize = pageRequest.limit() + 1;
		var pageable = PageRequest.of(0, fetchSize);

		List<FeatureFlag> rows;
		if (pageRequest.cursor() == null || pageRequest.cursor().isBlank()) {
			rows = jpa.findFirstPage(environment, status, pageable);
		} else {
			CreatedAtIdCursor cursor = CreatedAtIdCursor.decode(pageRequest.cursor());
			rows = jpa.findPageAfterCursor(
					environment,
					status,
					cursor.createdAt(),
					cursor.id(),
					pageable
			);
		}

		boolean hasMore = rows.size() > pageRequest.limit();
		List<FeatureFlag> items = hasMore ? rows.subList(0, pageRequest.limit()) : rows;

		String nextCursor = null;
		if (hasMore && !items.isEmpty()) {
			FeatureFlag last = items.getLast();
			nextCursor = new CreatedAtIdCursor(last.getCreatedAt(), last.getId()).encode();
		}

		return CursorPage.of(items, nextCursor, hasMore);
	}
}
