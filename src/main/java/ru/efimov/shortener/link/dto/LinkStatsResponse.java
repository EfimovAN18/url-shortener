package ru.efimov.shortener.link.dto;

import java.time.Instant;
import java.util.List;
import ru.efimov.shortener.click.DailyCount;
import ru.efimov.shortener.click.KeyCount;

public record LinkStatsResponse(
        String code,
        String originalUrl,
        long totalClicks,
        Instant lastClickAt,
        List<DailyCount> byDay,
        List<KeyCount> byBrowser,
        List<KeyCount> bySource
) {
}
