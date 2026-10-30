package ru.efimov.shortener.util;

import java.net.URI;
import java.util.Locale;

/**
 * Извлекает домен источника перехода из заголовка Referer.
 */
public final class RefererParser {

    public static final String DIRECT = "direct";

    private RefererParser() {
    }

    public static String source(String referer) {
        if (referer == null || referer.isBlank()) {
            return DIRECT;
        }
        try {
            String host = URI.create(referer.trim()).getHost();
            if (host == null || host.isBlank()) {
                return DIRECT;
            }
            host = host.toLowerCase(Locale.ROOT);
            return host.startsWith("www.") ? host.substring(4) : host;
        } catch (IllegalArgumentException e) {
            return DIRECT;
        }
    }
}
