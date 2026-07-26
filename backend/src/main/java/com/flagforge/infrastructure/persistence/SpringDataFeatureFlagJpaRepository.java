package com.flagforge.infrastructure.persistence;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FeatureFlag;
import com.flagforge.domain.flag.FlagStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFeatureFlagJpaRepository extends JpaRepository<FeatureFlag, UUID> {

	Optional<FeatureFlag> findByKeyAndEnvironment(String key, Environment environment);

	boolean existsByKeyAndEnvironment(String key, Environment environment);

	@Query("""
			SELECT f FROM FeatureFlag f
			WHERE (:environment IS NULL OR f.environment = :environment)
			  AND (:status IS NULL OR f.status = :status)
			ORDER BY f.createdAt DESC, f.id DESC
			""")
	List<FeatureFlag> findFirstPage(
			@Param("environment") Environment environment,
			@Param("status") FlagStatus status,
			Pageable pageable
	);

	@Query("""
			SELECT f FROM FeatureFlag f
			WHERE (:environment IS NULL OR f.environment = :environment)
			  AND (:status IS NULL OR f.status = :status)
			  AND (
			        f.createdAt < :cursorCreatedAt
			        OR (f.createdAt = :cursorCreatedAt AND f.id < :cursorId)
			      )
			ORDER BY f.createdAt DESC, f.id DESC
			""")
	List<FeatureFlag> findPageAfterCursor(
			@Param("environment") Environment environment,
			@Param("status") FlagStatus status,
			@Param("cursorCreatedAt") Instant cursorCreatedAt,
			@Param("cursorId") UUID cursorId,
			Pageable pageable
	);
}
