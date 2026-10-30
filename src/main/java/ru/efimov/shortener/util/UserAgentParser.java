package ru.efimov.shortener.util;

import java.util.Locale;

/**
 * Упрощённое определение браузера по заголовку User-Agent.
 * <p>
 * Порядок проверок важен: многие браузеры на движке Chromium тоже содержат слово «Chrome»,
 * а Chrome содержит слово «Safari», поэтому более специфичные варианты проверяются раньше.
 */
public final class UserAgentParser {

    public static final String UNKNOWN = "Other";

    private UserAgentParser() {
    }

    public static String browser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return UNKNOWN;
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        if (ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")) {
            return "Bot";
        }
        if (ua.contains("yabrowser")) {
            return "Yandex Browser";
        }
        if (ua.contains("edg/") || ua.contains("edge/")) {
            return "Edge";
        }
        if (ua.contains("opr/") || ua.contains("opera")) {
            return "Opera";
        }
        if (ua.contains("firefox/")) {
            return "Firefox";
        }
        if (ua.contains("chrome/") || ua.contains("crios/")) {
            return "Chrome";
        }
        if (ua.contains("safari/")) {
            return "Safari";
        }
        if (ua.contains("curl/")) {
            return "curl";
        }
        return UNKNOWN;
    }
}
