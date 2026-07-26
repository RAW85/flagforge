package com.flagforge.domain.repository;

import com.flagforge.domain.saga.RolloutSaga;
import com.flagforge.domain.saga.SagaStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RolloutSagaRepository {

	RolloutSaga save(RolloutSaga saga);

	Optional<RolloutSaga> findById(UUID id);

	List<RolloutSaga> findByFlagId(UUID flagId);

	List<RolloutSaga> findByStatus(SagaStatus status);

	boolean existsByFlagIdAndStatus(UUID flagId, SagaStatus status);
}
