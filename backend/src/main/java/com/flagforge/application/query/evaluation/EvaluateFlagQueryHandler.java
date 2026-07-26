package com.flagforge.application.query.evaluation;

import com.flagforge.domain.evaluation.EvaluationResult;
import com.flagforge.domain.evaluation.FlagEvaluation;
import com.flagforge.domain.evaluation.FlagEvaluationEngine;
import com.flagforge.domain.evaluation.FlagSnapshot;
import com.flagforge.domain.exception.ResourceNotFoundException;
import com.flagforge.domain.repository.FlagCache;
import com.flagforge.domain.repository.FlagEvaluationRepository;
import com.flagforge.domain.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates flag evaluation: cache → DB fallback → engine → optional history row.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluateFlagQueryHandler {

	private final FeatureFlagRepository featureFlagRepository;
	private final FlagCache flagCache;
	private final FlagEvaluationEngine evaluationEngine;
	private final FlagEvaluationRepository flagEvaluationRepository;

	/**
	 * Resolves a {@link FlagSnapshot} (cache miss loads DB and warms cache), then evaluates.
	 * When {@code query.record()} is true, persists a {@link FlagEvaluation} history row.
	 */
	@Transactional
	public EvaluationResult handle(EvaluateFlagQuery query) {
		FlagSnapshot snapshot = resolveSnapshot(query.flagKey().trim(), query.environment());
		EvaluationResult result = evaluationEngine.evaluate(snapshot, query.subjectId());

		if (query.record()) {
			flagEvaluationRepository.save(FlagEvaluation.builder()
					.flagId(result.flagId())
					.flagKey(result.flagKey())
					.subjectId(result.subjectId())
					.contextJson(query.contextJson())
					.resultValue(result.value())
					.reason(result.reason().name())
					.build());
		}

		log.debug(
				"Evaluated flag={} env={} subject={} value={} reason={} cache={}",
				result.flagKey(),
				result.environment(),
				result.subjectId(),
				result.value(),
				result.reason(),
				flagCache.backend()
		);

		return result;
	}

	/** Cache first; on miss load from DB, put into cache, or throw not-found. */
	private FlagSnapshot resolveSnapshot(String flagKey, com.flagforge.domain.flag.Environment environment) {
		return flagCache.get(flagKey, environment)
				.orElseGet(() -> {
					FlagSnapshot loaded = featureFlagRepository
							.findByKeyAndEnvironment(flagKey, environment)
							.map(FlagSnapshot::from)
							.orElseThrow(() -> new ResourceNotFoundException(
									"FeatureFlag not found: key=%s environment=%s"
											.formatted(flagKey, environment)
							));
					flagCache.put(loaded);
					return loaded;
				});
	}
}
