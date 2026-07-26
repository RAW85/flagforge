package com.flagforge.domain.evaluation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flagforge.domain.flag.FlagStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

/**
 * Pure evaluation logic — no I/O.
 * <p>
 * Sticky bucketing: {@code CRC32(flagKey:subjectId) % 100} yields 0–99.
 * Percentage flags treat {@code bucket < percentage} as in-rollout.
 */
@Component
@Slf4j
public class FlagEvaluationEngine {

	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Evaluates {@code flag} for {@code subjectId}. Disabled/non-ACTIVE flags return the default.
	 */
	public EvaluationResult evaluate(FlagSnapshot flag, String subjectId) {
		String subject = subjectId == null ? "" : subjectId.trim();
		int bucket = stickyBucket(flag.key(), subject);

		if (!isActivelyEnabled(flag)) {
			return result(flag, subject, safeDefault(flag), false, EvaluationReason.FLAG_DISABLED, bucket);
		}

		return switch (flag.flagType()) {
			case BOOLEAN -> evaluateBoolean(flag, subject, bucket);
			case PERCENTAGE -> evaluatePercentage(flag, subject, bucket);
			case MULTIVARIATE -> evaluateMultivariate(flag, subject, bucket);
		};
	}

	/** Enabled + ACTIVE boolean → {@code value="true"}, {@code enabled=true}. */
	private EvaluationResult evaluateBoolean(FlagSnapshot flag, String subject, int bucket) {
		return result(flag, subject, "true", true, EvaluationReason.BOOLEAN_ENABLED, bucket);
	}

	/** In-rollout when {@code stickyBucket < percentage}; same subject always lands in the same bucket. */
	private EvaluationResult evaluatePercentage(FlagSnapshot flag, String subject, int bucket) {
		int percentage = flag.percentage() == null ? 0 : flag.percentage();
		boolean inRollout = bucket < percentage;
		if (inRollout) {
			return result(flag, subject, "true", true, EvaluationReason.PERCENTAGE_IN, bucket);
		}
		return result(flag, subject, "false", false, EvaluationReason.PERCENTAGE_OUT, bucket);
	}

	/**
	 * rulesJson format:
	 * <pre>
	 * {"variants":[{"name":"control","weight":50},{"name":"treatment","weight":50}]}
	 * </pre>
	 */
	private EvaluationResult evaluateMultivariate(FlagSnapshot flag, String subject, int bucket) {
		List<Variant> variants = parseVariants(flag.rulesJson());
		if (variants.isEmpty()) {
			return result(flag, subject, safeDefault(flag), true, EvaluationReason.DEFAULT_VALUE, bucket);
		}

		int totalWeight = variants.stream().mapToInt(Variant::weight).sum();
		if (totalWeight <= 0) {
			return result(flag, subject, variants.getFirst().name(), true, EvaluationReason.DEFAULT_VALUE, bucket);
		}

		int scaled = (int) Math.floor((bucket / 100.0) * totalWeight);
		int cumulative = 0;
		for (Variant variant : variants) {
			cumulative += variant.weight();
			if (scaled < cumulative) {
				return result(flag, subject, variant.name(), true, EvaluationReason.VARIANT_MATCH, bucket);
			}
		}

		return result(flag, subject, variants.getLast().name(), true, EvaluationReason.VARIANT_MATCH, bucket);
	}

	private boolean isActivelyEnabled(FlagSnapshot flag) {
		return flag.enabled() && flag.status() == FlagStatus.ACTIVE;
	}

	/**
	 * Sticky 0–99 bucket shared across evaluations for the same flag+subject.
	 */
	public int stickyBucket(String flagKey, String subjectId) {
		CRC32 crc = new CRC32();
		crc.update((flagKey + ":" + subjectId).getBytes(StandardCharsets.UTF_8));
		return (int) Math.floorMod(crc.getValue(), 100L);
	}

	private String safeDefault(FlagSnapshot flag) {
		return flag.defaultValue() != null && !flag.defaultValue().isBlank()
				? flag.defaultValue()
				: "false";
	}

	private EvaluationResult result(
			FlagSnapshot flag,
			String subject,
			String value,
			boolean enabled,
			EvaluationReason reason,
			Integer bucket
	) {
		return new EvaluationResult(
				flag.id(),
				flag.key(),
				flag.environment(),
				flag.flagType(),
				subject,
				value,
				enabled,
				reason,
				bucket
		);
	}

	private List<Variant> parseVariants(String rulesJson) {
		if (rulesJson == null || rulesJson.isBlank()) {
			return List.of();
		}
		try {
			RulesDocument doc = objectMapper.readValue(rulesJson, RulesDocument.class);
			if (doc.variants() == null || doc.variants().isEmpty()) {
				return List.of();
			}
			List<Variant> variants = new ArrayList<>();
			for (VariantDto dto : doc.variants()) {
				String name = dto.name() != null ? dto.name() : dto.key();
				int weight = dto.weight() == null ? 0 : dto.weight();
				if (name != null && !name.isBlank() && weight > 0) {
					variants.add(new Variant(name, weight));
				}
			}
			return variants;
		} catch (Exception ex) {
			log.warn("Invalid multivariate rulesJson, falling back to default: {}", ex.getMessage());
			return List.of();
		}
	}

	private record Variant(String name, int weight) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record RulesDocument(List<VariantDto> variants) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record VariantDto(String name, String key, Integer weight) {
	}
}
