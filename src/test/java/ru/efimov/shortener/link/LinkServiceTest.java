package ru.efimov.shortener.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.efimov.shortener.click.Click;
import ru.efimov.shortener.click.ClickRepository;
import ru.efimov.shortener.config.ShortenerProperties;
import ru.efimov.shortener.link.dto.CreateLinkRequest;
import ru.efimov.shortener.link.dto.LinkResponse;
import ru.efimov.shortener.util.CodeGenerator;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-24T21:30:00Z");

    @Mock
    private LinkRepository linkRepository;
    @Mock
    private ClickRepository clickRepository;
    @Mock
    private LinkCache linkCache;
    @Mock
    private CodeGenerator codeGenerator;

    private LinkService service;

    @BeforeEach
    void setUp() {
        ShortenerProperties properties =
                new ShortenerProperties("http://sho.rt/", 7, 365, ZoneId.of("Europe/Moscow"));
        service = new LinkService(linkRepository, clickRepository, linkCache, codeGenerator,
                properties, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsLinkWithGeneratedCodeAndRetriesOnCollision() {
        when(codeGenerator.generate(7)).thenReturn("taken01", "free001");
        when(linkRepository.existsByCode("taken01")).thenReturn(true);
        when(linkRepository.existsByCode("free001")).thenReturn(false);
        when(linkRepository.save(any(Link.class))).thenAnswer(inv -> inv.getArgument(0));

        LinkResponse response = service.create(new CreateLinkRequest("https://example.com", null, 30));

        assertThat(response.code()).isEqualTo("free001");
        assertThat(response.shortUrl()).isEqualTo("http://sho.rt/free001");
        assertThat(response.expiresAt()).isEqualTo(Instant.parse("2026-11-23T21:30:00Z"));
    }

    @Test
    void createsLinkWithAlias() {
        when(linkRepository.existsByCode("my-link")).thenReturn(false);
        when(linkRepository.saveAndFlush(any(Link.class))).thenAnswer(inv -> inv.getArgument(0));

        LinkResponse response = service.create(new CreateLinkRequest("https://example.com", "my-link", null));

        assertThat(response.code()).isEqualTo("my-link");
        assertThat(response.expiresAt()).isNull();
        verify(codeGenerator, never()).generate(7);
    }

    @Test
    void rejectsTakenAndReservedAliases() {
        when(linkRepository.existsByCode("busy")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateLinkRequest("https://example.com", "busy", null)))
                .isInstanceOf(LinkExceptions.AliasTaken.class);
        assertThatThrownBy(() -> service.create(new CreateLinkRequest("https://example.com", "API", null)))
                .isInstanceOf(LinkExceptions.AliasTaken.class);
    }

    @Test
    void rejectsInvalidUrlAndTooLongTtl() {
        assertThatThrownBy(() -> service.create(new CreateLinkRequest("example.com", null, null)))
                .isInstanceOf(LinkExceptions.BadRequest.class);
        assertThatThrownBy(() -> service.create(new CreateLinkRequest("https://example.com", null, 1000)))
                .isInstanceOf(LinkExceptions.BadRequest.class);
    }

    @Test
    void registersClickAndUsesMoscowDate() {
        // 21:30 UTC — это уже 00:30 следующего дня по Москве
        when(linkCache.find("abc1234")).thenReturn(new LinkTarget(1L, "https://example.com", null));
        when(linkRepository.getReferenceById(1L)).thenReturn(new Link("abc1234", "https://example.com", NOW, null));

        String target = service.registerClick("abc1234", "curl/8.5.0", "https://www.google.com/");

        assertThat(target).isEqualTo("https://example.com");
        ArgumentCaptor<Click> captor = ArgumentCaptor.forClass(Click.class);
        verify(clickRepository).save(captor.capture());
        Click click = captor.getValue();
        assertThat(click.getClickDate()).isEqualTo(LocalDate.of(2026, 10, 25));
        assertThat(click.getBrowser()).isEqualTo("curl");
        assertThat(click.getSource()).isEqualTo("google.com");
    }

    @Test
    void failsForUnknownLink() {
        when(linkCache.find(anyString())).thenReturn(null);
        assertThatThrownBy(() -> service.registerClick("nope123", null, null))
                .isInstanceOf(LinkExceptions.NotFound.class);
    }

    @Test
    void failsForExpiredLinkWithoutRecordingClick() {
        when(linkCache.find("old1234")).thenReturn(new LinkTarget(2L, "https://example.com", NOW.minusSeconds(1)));
        assertThatThrownBy(() -> service.registerClick("old1234", null, null))
                .isInstanceOf(LinkExceptions.Expired.class);
        verify(clickRepository, never()).save(any());
    }
}
