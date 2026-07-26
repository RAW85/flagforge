package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.saga.RolloutSaga;
import com.flagforge.domain.saga.SagaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataRolloutSagaJpaRepository extends JpaRepository<RolloutSaga, UUID> {

	List<RolloutSaga> findByFlagIdOrderByCreatedAtDesc(UUID flagId);

	List<RolloutSaga> findByStatus(SagaStatus status);

	boolean existsByFlagIdAndStatus(UUID flagId, SagaStatus status);
}
