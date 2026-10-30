package ru.efimov.shortener.click;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClickRepository extends JpaRepository<Click, Long> {

    long countByLinkId(Long linkId);

    @Query("select max(c.clickedAt) from Click c where c.link.id = :linkId")
    Instant findLastClickAt(@Param("linkId") Long linkId);

    @Query("""
            select new ru.efimov.shortener.click.DailyCount(c.clickDate, count(c))
            from Click c
            where c.link.id = :linkId
            group by c.clickDate
            order by c.clickDate
            """)
    List<DailyCount> countByDay(@Param("linkId") Long linkId);

    @Query("""
            select new ru.efimov.shortener.click.KeyCount(c.browser, count(c))
            from Click c
            where c.link.id = :linkId
            group by c.browser
            order by count(c) desc
            """)
    List<KeyCount> countByBrowser(@Param("linkId") Long linkId);

    @Query("""
            select new ru.efimov.shortener.click.KeyCount(c.source, count(c))
            from Click c
            where c.link.id = :linkId
            group by c.source
            order by count(c) desc
            """)
    List<KeyCount> countBySource(@Param("linkId") Long linkId);
}
