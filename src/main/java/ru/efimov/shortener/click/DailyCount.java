package ru.efimov.shortener.click;

import java.time.LocalDate;

/** Количество переходов за один день. */
public record DailyCount(LocalDate date, Long clicks) {
}
