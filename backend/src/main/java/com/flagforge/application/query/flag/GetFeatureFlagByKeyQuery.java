package com.flagforge.application.query.flag;

import com.flagforge.domain.flag.Environment;

public record GetFeatureFlagByKeyQuery(String key, Environment environment) {
}
