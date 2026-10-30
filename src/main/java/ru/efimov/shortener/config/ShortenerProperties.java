package ru.efimov.shortener.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Настройки сервиса из application.yml (префикс {@code shortener}).
 *
 * @param baseUrl    адрес, с которого начинаются короткие ссылки, например http://localhost:8080
 * @param codeLength длина автоматически сгенерированного кода
 * @param maxTtlDays максимальный срок жизни ссылки в днях
 * @param statsZone  часовой пояс, по которому переходы раскладываются по дням
 */
@Validated
@ConfigurationProperties(prefix = "shortener")
public record ShortenerProperties(
        @NotBlank @DefaultValue("http://localhost:8080") String baseUrl,
        @Min(4) @Max(16) @DefaultValue("7") int codeLength,
        @Min(1) @DefaultValue("365") int maxTtlDays,
        @DefaultValue("Europe/Moscow") ZoneId statsZone
) {

    /** Базовый адрес без завершающего слэша. */
    public String normalizedBaseUrl() {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
