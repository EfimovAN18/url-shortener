package ru.efimov.shortener.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Проверки адресов и пользовательских алиасов.
 */
public final class UrlValidator {

    /** Алиас: латиница, цифры, дефис и подчёркивание, от 3 до 32 символов. */
    private static final Pattern ALIAS = Pattern.compile("^[A-Za-z0-9_-]{3,32}$");

    public static final int MAX_URL_LENGTH = 2048;

    private UrlValidator() {
    }

    /**
     * Ссылка считается корректной, если это абсолютный адрес http или https с указанным хостом.
     */
    public static boolean isValidHttpUrl(String value) {
        if (value == null || value.isBlank() || value.length() > MAX_URL_LENGTH) {
            return false;
        }
        try {
            URI uri = new URI(value.trim());
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }
            String lower = scheme.toLowerCase(Locale.ROOT);
            return (lower.equals("http") || lower.equals("https"))
                    && uri.getHost() != null
                    && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static boolean isValidAlias(String alias) {
        return alias != null && ALIAS.matcher(alias).matches();
    }
}
