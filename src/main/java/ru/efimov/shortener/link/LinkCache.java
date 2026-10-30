package ru.efimov.shortener.link;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Кэш «код → адрес». Вынесен в отдельный бин: аннотации кэширования работают через прокси
 * и не срабатывают, если метод вызывается изнутри того же класса.
 */
@Component
public class LinkCache {

    public static final String CACHE_NAME = "links";

    private final LinkRepository linkRepository;

    public LinkCache(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Cacheable(cacheNames = CACHE_NAME, unless = "#result == null")
    @Transactional(readOnly = true)
    public LinkTarget find(String code) {
        return linkRepository.findByCode(code)
                .map(link -> new LinkTarget(link.getId(), link.getOriginalUrl(), link.getExpiresAt()))
                .orElse(null);
    }

    @CacheEvict(cacheNames = CACHE_NAME)
    public void evict(String code) {
        // тело не нужно: запись удаляет сама аннотация
    }
}
