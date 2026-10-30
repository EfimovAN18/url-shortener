package ru.efimov.shortener.link;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.efimov.shortener.click.Click;
import ru.efimov.shortener.click.ClickRepository;
import ru.efimov.shortener.config.ShortenerProperties;
import ru.efimov.shortener.link.dto.CreateLinkRequest;
import ru.efimov.shortener.link.dto.LinkResponse;
import ru.efimov.shortener.link.dto.LinkStatsResponse;
import ru.efimov.shortener.util.CodeGenerator;
import ru.efimov.shortener.util.RefererParser;
import ru.efimov.shortener.util.UrlValidator;
import ru.efimov.shortener.util.UserAgentParser;

@Service
public class LinkService {

    /** Коды, которые нельзя занять: они совпадают со служебными путями приложения. */
    static final Set<String> RESERVED = Set.of("api", "actuator", "swagger-ui", "v3", "error", "favicon.ico");

    private static final int MAX_GENERATION_ATTEMPTS = 10;

    private final LinkRepository linkRepository;
    private final ClickRepository clickRepository;
    private final LinkCache linkCache;
    private final CodeGenerator codeGenerator;
    private final ShortenerProperties properties;
    private final Clock clock;

    public LinkService(LinkRepository linkRepository,
                       ClickRepository clickRepository,
                       LinkCache linkCache,
                       CodeGenerator codeGenerator,
                       ShortenerProperties properties,
                       Clock clock) {
        this.linkRepository = linkRepository;
        this.clickRepository = clickRepository;
        this.linkCache = linkCache;
        this.codeGenerator = codeGenerator;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public LinkResponse create(CreateLinkRequest request) {
        String url = request.url().trim();
        if (!UrlValidator.isValidHttpUrl(url)) {
            throw new LinkExceptions.BadRequest("Нужен абсолютный адрес, начинающийся с http:// или https://");
        }
        if (request.ttlDays() != null && request.ttlDays() > properties.maxTtlDays()) {
            throw new LinkExceptions.BadRequest(
                    "Срок жизни ссылки не может быть больше " + properties.maxTtlDays() + " дней");
        }

        Instant now = clock.instant();
        Instant expiresAt = request.ttlDays() == null ? null : now.plus(Duration.ofDays(request.ttlDays()));

        if (request.alias() != null && !request.alias().isBlank()) {
            return createWithAlias(request.alias().trim(), url, now, expiresAt);
        }
        Link link = linkRepository.save(new Link(generateUniqueCode(), url, now, expiresAt));
        return toResponse(link);
    }

    private LinkResponse createWithAlias(String alias, String url, Instant now, Instant expiresAt) {
        if (!UrlValidator.isValidAlias(alias)) {
            throw new LinkExceptions.BadRequest(
                    "Алиас может содержать латиницу, цифры, - и _, длина от 3 до 32 символов");
        }
        if (RESERVED.contains(alias.toLowerCase(Locale.ROOT)) || linkRepository.existsByCode(alias)) {
            throw new LinkExceptions.AliasTaken(alias);
        }
        try {
            // saveAndFlush, чтобы нарушение уникальности (гонка двух запросов) поймать прямо здесь
            Link link = linkRepository.saveAndFlush(new Link(alias, url, now, expiresAt));
            return toResponse(link);
        } catch (DataIntegrityViolationException e) {
            throw new LinkExceptions.AliasTaken(alias);
        }
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = codeGenerator.generate(properties.codeLength());
            if (!linkRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Не удалось подобрать свободный код, попробуйте ещё раз");
    }

    /**
     * Находит адрес для редиректа и записывает переход.
     */
    @Transactional
    public String registerClick(String code, String userAgent, String referer) {
        LinkTarget target = linkCache.find(code);
        if (target == null) {
            throw new LinkExceptions.NotFound(code);
        }
        Instant now = clock.instant();
        if (target.isExpired(now)) {
            throw new LinkExceptions.Expired(code);
        }
        LocalDate day = LocalDate.ofInstant(now, properties.statsZone());
        clickRepository.save(new Click(
                linkRepository.getReferenceById(target.id()),
                now,
                day,
                UserAgentParser.browser(userAgent),
                truncate(RefererParser.source(referer), 255)));
        return target.originalUrl();
    }

    @Transactional(readOnly = true)
    public LinkResponse get(String code) {
        return toResponse(findLink(code));
    }

    @Transactional(readOnly = true)
    public LinkStatsResponse stats(String code) {
        Link link = findLink(code);
        Long id = link.getId();
        return new LinkStatsResponse(
                link.getCode(),
                link.getOriginalUrl(),
                clickRepository.countByLinkId(id),
                clickRepository.findLastClickAt(id),
                clickRepository.countByDay(id),
                clickRepository.countByBrowser(id),
                clickRepository.countBySource(id));
    }

    @Transactional
    public void delete(String code) {
        Link link = findLink(code);
        // переходы удалятся каскадно на уровне базы (ON DELETE CASCADE)
        linkRepository.delete(link);
        linkCache.evict(code);
    }

    private Link findLink(String code) {
        return linkRepository.findByCode(code).orElseThrow(() -> new LinkExceptions.NotFound(code));
    }

    private LinkResponse toResponse(Link link) {
        return new LinkResponse(
                link.getCode(),
                properties.normalizedBaseUrl() + "/" + link.getCode(),
                link.getOriginalUrl(),
                link.getCreatedAt(),
                link.getExpiresAt());
    }

    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
