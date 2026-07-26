package com.flagforge.domain.repository;

import com.flagforge.domain.common.CursorPage;
import com.flagforge.domain.common.CursorPageRequest;
import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FeatureFlag;
import com.flagforge.domain.flag.FlagStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for feature flag persistence.
 */
public interface FeatureFlagRepository {

	FeatureFlag save(FeatureFlag flag);

	Optional<FeatureFlag> findById(UUID id);

	Optional<FeatureFlag> findByKeyAndEnvironment(String key, Environment environment);

	boolean existsByKeyAndEnvironment(String key, Environment environment);

	void delete(FeatureFlag flag);

	/**
	 * Cursor-paginated list, optionally filtered by environment and/or status.
	 */
	CursorPage<FeatureFlag> findPage(
			CursorPageRequest pageRequest,
			Environment environment,
			FlagStatus status
	);
}
