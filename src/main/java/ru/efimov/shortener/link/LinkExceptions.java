package ru.efimov.shortener.link;

/**
 * Исключения предметной области. Обработчик в web-слое превращает их в ответы с нужным HTTP-статусом.
 */
public final class LinkExceptions {

    private LinkExceptions() {
    }

    /** 404: ссылки с таким кодом нет. */
    public static class NotFound extends RuntimeException {
        public NotFound(String code) {
            super("Ссылка с кодом '" + code + "' не найдена");
        }
    }

    /** 410: ссылка была, но срок её жизни истёк. */
    public static class Expired extends RuntimeException {
        public Expired(String code) {
            super("Срок действия ссылки '" + code + "' истёк");
        }
    }

    /** 409: такой алиас уже занят. */
    public static class AliasTaken extends RuntimeException {
        public AliasTaken(String alias) {
            super("Алиас '" + alias + "' уже занят");
        }
    }

    /** 400: некорректные входные данные. */
    public static class BadRequest extends RuntimeException {
        public BadRequest(String message) {
            super(message);
        }
    }
}
