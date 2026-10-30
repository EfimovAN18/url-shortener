package ru.efimov.shortener.click;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import ru.efimov.shortener.link.Link;

/**
 * Один переход по короткой ссылке.
 * <p>
 * Дата перехода хранится отдельным столбцом, чтобы группировка по дням была простым
 * GROUP BY по индексу и одинаково работала в PostgreSQL и в H2 (в тестах).
 */
@Entity
@Table(name = "clicks")
public class Click {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    @Column(name = "click_date", nullable = false)
    private LocalDate clickDate;

    @Column(nullable = false, length = 64)
    private String browser;

    @Column(nullable = false, length = 255)
    private String source;

    protected Click() {
        // нужен JPA
    }

    public Click(Link link, Instant clickedAt, LocalDate clickDate, String browser, String source) {
        this.link = link;
        this.clickedAt = clickedAt;
        this.clickDate = clickDate;
        this.browser = browser;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public Link getLink() {
        return link;
    }

    public Instant getClickedAt() {
        return clickedAt;
    }

    public LocalDate getClickDate() {
        return clickDate;
    }

    public String getBrowser() {
        return browser;
    }

    public String getSource() {
        return source;
    }
}
