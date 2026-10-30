package ru.efimov.shortener.link.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateLinkRequest(
        @Schema(description = "Исходный адрес (http или https)", example = "https://github.com/EfimovAN18")
        @NotBlank @Size(max = 2048)
        String url,

        @Schema(description = "Свой код вместо случайного: латиница, цифры, - и _, от 3 до 32 символов",
                example = "my-github", nullable = true)
        String alias,

        @Schema(description = "Срок жизни ссылки в днях; если не указан, ссылка бессрочная",
                example = "30", nullable = true)
        @Positive
        Integer ttlDays
) {
}
