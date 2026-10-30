package ru.efimov.shortener.click;

/** Количество переходов в разрезе какого-то признака: браузера или источника. */
public record KeyCount(String key, Long clicks) {
}
