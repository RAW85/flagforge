package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.evaluation.FlagEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataFlagEvaluationJpaRepository extends JpaRepository<FlagEvaluation, UUID> {
}
