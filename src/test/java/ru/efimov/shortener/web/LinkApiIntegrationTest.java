package ru.efimov.shortener.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Полный сценарий через HTTP: создание, переходы, статистика, удаление.
 * Поднимается весь контекст Spring с базой H2 и миграциями Flyway.
 */
@SpringBootTest
@AutoConfigureMockMvc
class LinkApiIntegrationTest {

    private static final String CHROME_UA =
            "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 Chrome/130.0 Safari/537.36";

    @Autowired
    private MockMvc mvc;

    @Test
    void fullLifecycle() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": "https://github.com/EfimovAN18", "alias": "gh-profile"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/links/gh-profile"))
                .andExpect(jsonPath("$.shortUrl").value("http://sho.rt/gh-profile"));

        mvc.perform(get("/gh-profile").header(HttpHeaders.USER_AGENT, CHROME_UA)
                        .header(HttpHeaders.REFERER, "https://t.me/some_channel"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://github.com/EfimovAN18"));
        mvc.perform(get("/gh-profile").header(HttpHeaders.USER_AGENT, "curl/8.5.0"))
                .andExpect(status().isFound());

        mvc.perform(get("/api/links/gh-profile/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClicks").value(2))
                .andExpect(jsonPath("$.byDay", hasSize(1)))
                .andExpect(jsonPath("$.byDay[0].clicks").value(2))
                .andExpect(jsonPath("$.byBrowser", hasSize(2)))
                .andExpect(jsonPath("$.bySource[?(@.key == 't.me')].clicks").value(1))
                .andExpect(jsonPath("$.bySource[?(@.key == 'direct')].clicks").value(1));

        mvc.perform(delete("/api/links/gh-profile")).andExpect(status().isNoContent());
        mvc.perform(get("/gh-profile")).andExpect(status().isNotFound());
        mvc.perform(get("/api/links/gh-profile")).andExpect(status().isNotFound());
    }

    @Test
    void generatesCodeWhenAliasIsMissing() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"https://example.com\", \"ttlDays\": 7}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").isString())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void returnsConflictForDuplicateAlias() throws Exception {
        String body = "{\"url\": \"https://example.com\", \"alias\": \"dup-alias\"}";
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/links").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Алиас занят"));
    }

    @Test
    void returnsBadRequestForInvalidInput() throws Exception {
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"not-a-url\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}
