package ru.efimov.shortener.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class UtilTest {

    @Test
    void codeGeneratorProducesCodesOfRequestedLengthFromAlphabet() {
        CodeGenerator generator = new CodeGenerator(new Random(42));
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String code = generator.generate(7);
            assertThat(code).hasSize(7);
            assertThat(code.chars()).allMatch(ch -> CodeGenerator.ALPHABET.indexOf(ch) >= 0);
            codes.add(code);
        }
        assertThat(codes).hasSize(1000);
    }

    @Test
    void codeGeneratorRejectsNonPositiveLength() {
        assertThatThrownBy(() -> new CodeGenerator().generate(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/EfimovAN18",
            "http://example.com",
            "HTTPS://example.com/path?q=1#top",
            "https://xn--80aswg.xn--p1ai/"
    })
    void acceptsHttpUrls(String url) {
        assertThat(UrlValidator.isValidHttpUrl(url)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "github.com", "ftp://example.com", "javascript:alert(1)", "https://", "http:// bad"})
    void rejectsInvalidUrls(String url) {
        assertThat(UrlValidator.isValidHttpUrl(url)).isFalse();
    }

    @Test
    void validatesAlias() {
        assertThat(UrlValidator.isValidAlias("my-github_1")).isTrue();
        assertThat(UrlValidator.isValidAlias("ab")).isFalse();
        assertThat(UrlValidator.isValidAlias("привет")).isFalse();
        assertThat(UrlValidator.isValidAlias("a".repeat(33))).isFalse();
        assertThat(UrlValidator.isValidAlias(null)).isFalse();
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0 Safari/537.36|Chrome",
            "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 Chrome/130.0 Safari/537.36 Edg/130.0|Edge",
            "Mozilla/5.0 (Windows NT 10.0) AppleWebKit/537.36 Chrome/128.0 YaBrowser/24.10 Safari/537.36|Yandex Browser",
            "Mozilla/5.0 (X11; Linux x86_64; rv:131.0) Gecko/20100101 Firefox/131.0|Firefox",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 Version/17.0 Mobile/15E148 Safari/604.1|Safari",
            "Mozilla/5.0 AppleWebKit/537.36 Chrome/130.0 Safari/537.36 OPR/114.0|Opera",
            "curl/8.5.0|curl",
            "Googlebot/2.1|Bot"
    })
    void detectsBrowser(String userAgent, String expected) {
        assertThat(UserAgentParser.browser(userAgent)).isEqualTo(expected);
    }

    @Test
    void unknownBrowserForEmptyUserAgent() {
        assertThat(UserAgentParser.browser(null)).isEqualTo(UserAgentParser.UNKNOWN);
        assertThat(UserAgentParser.browser("")).isEqualTo(UserAgentParser.UNKNOWN);
    }

    @Test
    void extractsRefererSource() {
        assertThat(RefererParser.source("https://www.Google.com/search?q=x")).isEqualTo("google.com");
        assertThat(RefererParser.source("https://t.me/channel/1")).isEqualTo("t.me");
        assertThat(RefererParser.source(null)).isEqualTo(RefererParser.DIRECT);
        assertThat(RefererParser.source("not a url")).isEqualTo(RefererParser.DIRECT);
    }
}
