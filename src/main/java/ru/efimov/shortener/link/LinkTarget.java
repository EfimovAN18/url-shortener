package ru.efimov.shortener.link;

import java.io.Serializable;
import java.time.Instant;

/**
 * Минимум данных, нужный для редиректа. Именно он лежит в кэше,
 * чтобы частые переходы не ходили в базу за всей сущностью.
 */
public record LinkTarget(Long id, String originalUrl, Instant expiresAt) implements Serializable {

    public boolean isExpired(Instant now) {
        return expiresAt != null && !now.isBefore(expiresAt);
    }
}
