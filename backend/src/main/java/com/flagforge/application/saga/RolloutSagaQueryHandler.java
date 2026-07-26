package com.flagforge.application.saga;

import com.flagforge.domain.exception.ResourceNotFoundException;
import com.flagforge.domain.repository.RolloutSagaRepository;
import com.flagforge.domain.saga.RolloutSaga;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Read-side lookups for rollout sagas (by id or flag). */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolloutSagaQueryHandler {

	private final RolloutSagaRepository rolloutSagaRepository;

	public RolloutSaga handle(GetRolloutSagaQuery query) {
		return rolloutSagaRepository.findById(query.id())
				.orElseThrow(() -> ResourceNotFoundException.of("RolloutSaga", query.id()));
	}

	public List<RolloutSaga> handle(ListRolloutSagasByFlagQuery query) {
		return rolloutSagaRepository.findByFlagId(query.flagId());
	}
}
