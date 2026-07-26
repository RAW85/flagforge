package com.flagforge.application.query.flag;

import com.flagforge.domain.common.CursorPage;
import com.flagforge.domain.exception.ResourceNotFoundException;
import com.flagforge.domain.flag.FeatureFlag;
import com.flagforge.domain.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read-side lookups for feature flags (by id, key+env, cursor page). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeatureFlagQueryHandler {

	private final FeatureFlagRepository featureFlagRepository;

	public FeatureFlag handle(GetFeatureFlagQuery query) {
		return featureFlagRepository.findById(query.id())
				.orElseThrow(() -> ResourceNotFoundException.of("FeatureFlag", query.id()));
	}

	public FeatureFlag handle(GetFeatureFlagByKeyQuery query) {
		return featureFlagRepository.findByKeyAndEnvironment(query.key(), query.environment())
				.orElseThrow(() -> new ResourceNotFoundException(
						"FeatureFlag not found: key=%s environment=%s"
								.formatted(query.key(), query.environment())
				));
	}

	public CursorPage<FeatureFlag> handle(ListFeatureFlagsQuery query) {
		return featureFlagRepository.findPage(
				query.pageRequest(),
				query.environment(),
				query.status()
		);
	}
}
