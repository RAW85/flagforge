package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.evaluation.FlagEvaluation;
import com.flagforge.domain.repository.FlagEvaluationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FlagEvaluationRepositoryAdapter implements FlagEvaluationRepository {

	private final SpringDataFlagEvaluationJpaRepository jpa;

	@Override
	public FlagEvaluation save(FlagEvaluation evaluation) {
		return jpa.save(evaluation);
	}
}
