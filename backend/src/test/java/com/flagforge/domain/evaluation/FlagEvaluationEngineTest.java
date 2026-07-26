package com.flagforge.domain.evaluation;

import com.flagforge.domain.flag.Environment;
import com.flagforge.domain.flag.FlagStatus;
import com.flagforge.domain.flag.FlagType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FlagEvaluationEngineTest {

	private FlagEvaluationEngine engine;

	@BeforeEach
	void setUp() {
		engine = new FlagEvaluationEngine();
	}

	@Test
	void stickyBucket_isStableForSameSubject() {
		int a = engine.stickyBucket("checkout", "user-1");
		int b = engine.stickyBucket("checkout", "user-1");
		assertThat(a).isEqualTo(b);
		assertThat(a).isBetween(0, 99);
	}

	@Test
	void disabledFlag_returnsDefault() {
		FlagSnapshot flag = snapshot(FlagType.BOOLEAN, false, FlagStatus.DRAFT, null, null);
		EvaluationResult result = engine.evaluate(flag, "user-1");
		assertThat(result.enabled()).isFalse();
		assertThat(result.reason()).isEqualTo(EvaluationReason.FLAG_DISABLED);
		assertThat(result.value()).isEqualTo("false");
	}

	@Test
	void enabledBoolean_returnsTrue() {
		FlagSnapshot flag = snapshot(FlagType.BOOLEAN, true, FlagStatus.ACTIVE, null, null);
		EvaluationResult result = engine.evaluate(flag, "user-1");
		assertThat(result.enabled()).isTrue();
		assertThat(result.reason()).isEqualTo(EvaluationReason.BOOLEAN_ENABLED);
		assertThat(result.value()).isEqualTo("true");
	}

	@Test
	void percentage_respectsBucketBoundary() {
		FlagSnapshot flag = snapshot(FlagType.PERCENTAGE, true, FlagStatus.ACTIVE, 0, null);
		EvaluationResult out = engine.evaluate(flag, "user-1");
		assertThat(out.reason()).isEqualTo(EvaluationReason.PERCENTAGE_OUT);
		assertThat(out.value()).isEqualTo("false");

		FlagSnapshot full = snapshot(FlagType.PERCENTAGE, true, FlagStatus.ACTIVE, 100, null);
		EvaluationResult in = engine.evaluate(full, "user-1");
		assertThat(in.reason()).isEqualTo(EvaluationReason.PERCENTAGE_IN);
		assertThat(in.value()).isEqualTo("true");
	}

	@Test
	void multivariate_selectsVariant() {
		String rules = """
				{"variants":[{"name":"control","weight":50},{"name":"treatment","weight":50}]}
				""";
		FlagSnapshot flag = snapshot(FlagType.MULTIVARIATE, true, FlagStatus.ACTIVE, null, rules);
		EvaluationResult result = engine.evaluate(flag, "user-42");
		assertThat(result.reason()).isIn(EvaluationReason.VARIANT_MATCH, EvaluationReason.DEFAULT_VALUE);
		assertThat(result.value()).isIn("control", "treatment");
	}

	private FlagSnapshot snapshot(
			FlagType type,
			boolean enabled,
			FlagStatus status,
			Integer percentage,
			String rulesJson
	) {
		return new FlagSnapshot(
				UUID.randomUUID(),
				"demo-flag",
				Environment.DEVELOPMENT,
				enabled,
				status,
				type,
				"false",
				percentage,
				rulesJson
		);
	}
}
