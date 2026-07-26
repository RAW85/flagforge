package com.flagforge.application.query.apikey;

import java.util.UUID;

/**
 * @param ownerId when non-null, filter to keys owned by that user; otherwise list all (admin)
 */
public record ListApiKeysQuery(UUID ownerId) {
}
