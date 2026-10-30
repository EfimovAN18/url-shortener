package ru.efimov.shortener.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.efimov.shortener.link.LinkService;
import ru.efimov.shortener.link.dto.CreateLinkRequest;
import ru.efimov.shortener.link.dto.LinkResponse;
import ru.efimov.shortener.link.dto.LinkStatsResponse;

@RestController
@RequestMapping("/api/links")
@Tag(name = "Ссылки", description = "Создание коротких ссылок и статистика переходов")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping
    @Operation(summary = "Создать короткую ссылку")
    public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request) {
        LinkResponse created = linkService.create(request);
        return ResponseEntity.created(URI.create("/api/links/" + created.code())).body(created);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Информация о ссылке")
    public LinkResponse get(@PathVariable String code) {
        return linkService.get(code);
    }

    @GetMapping("/{code}/stats")
    @Operation(summary = "Статистика переходов: всего, по дням, по браузерам и источникам")
    public LinkStatsResponse stats(@PathVariable String code) {
        return linkService.stats(code);
    }

    @DeleteMapping("/{code}")
    @Operation(summary = "Удалить ссылку вместе с её статистикой")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        linkService.delete(code);
        return ResponseEntity.noContent().build();
    }
}
