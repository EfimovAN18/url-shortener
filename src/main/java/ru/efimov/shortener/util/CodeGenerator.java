package ru.efimov.shortener.util;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;

/**
 * Генерирует случайные короткие коды из алфавита Base62 (цифры, строчные и заглавные латинские буквы).
 * <p>
 * При длине 7 получается 62^7 ≈ 3,5 трлн вариантов, поэтому коллизии редки,
 * но сервис всё равно проверяет уникальность кода перед сохранением.
 */
public class CodeGenerator {

    public static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final RandomGenerator random;

    public CodeGenerator() {
        this(new SecureRandom());
    }

    public CodeGenerator(RandomGenerator random) {
        this.random = random;
    }

    public String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Длина кода должна быть положительной");
        }
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
