package ru.efimov.shortener.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.efimov.shortener.util.CodeGenerator;

@Configuration
public class AppConfig {

    /** Часы вынесены в бин, чтобы в тестах можно было подставить фиксированное время. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public CodeGenerator codeGenerator() {
        return new CodeGenerator();
    }
}
