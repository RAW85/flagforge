package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.repository.RolloutSagaRepository;
import com.flagforge.domain.saga.RolloutSaga;
import com.flagforge.domain.saga.SagaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RolloutSagaRepositoryAdapter implements RolloutSagaRepository {

	private final SpringDataRolloutSagaJpaRepository jpa;

	@Override
	public RolloutSaga save(RolloutSaga saga) {
		return jpa.save(saga);
	}

	@Override
	public Optional<RolloutSaga> findById(UUID id) {
		return jpa.findById(id);
	}

	@Override
	public List<RolloutSaga> findByFlagId(UUID flagId) {
		return jpa.findByFlagIdOrderByCreatedAtDesc(flagId);
	}

	@Override
	public List<RolloutSaga> findByStatus(SagaStatus status) {
		return jpa.findByStatus(status);
	}

	@Override
	public boolean existsByFlagIdAndStatus(UUID flagId, SagaStatus status) {
		return jpa.existsByFlagIdAndStatus(flagId, status);
	}
}
