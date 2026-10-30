# url-shortener

Сервис коротких ссылок с аналитикой переходов: REST API на Spring Boot, PostgreSQL, миграции Flyway, кэш, Docker.

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

## Возможности

- создание короткой ссылки со случайным кодом (Base62) или своим алиасом;
- срок жизни ссылки: после истечения сервис отвечает `410 Gone`;
- редирект `302` с записью каждого перехода;
- статистика: всего переходов, последний переход, разбивка по дням, браузерам и источникам (домен из `Referer`);
- кэш «код → адрес» в памяти (Caffeine), чтобы частые переходы не нагружали базу;
- ошибки в едином формате Problem Details (RFC 9457);
- документация API в Swagger UI.

## Стек

Java 21 · Spring Boot 3.5 (Web, Data JPA, Validation, Cache, Actuator) · PostgreSQL 16 · Flyway · Caffeine · springdoc-openapi · JUnit 5, Mockito, H2 · Docker Compose · GitHub Actions

## Запуск в IntelliJ IDEA

1. **File → Open** → выбрать папку проекта. IDEA распознает Maven и скачает зависимости.
2. Если IDEA попросит JDK: **File → Project Structure → SDK → Download JDK** → версия 21 (например, Eclipse Temurin).
3. В списке конфигураций запуска (вверху справа) уже есть готовые:
   - **PostgreSQL (Docker)** — поднимает базу (нужен запущенный Docker Desktop);
   - **Shortener App** — запускает приложение;
   - **All tests** — все тесты (Docker не нужен, тесты работают на H2);
   - **mvn verify** — полная сборка как в CI.
4. Запустить **PostgreSQL (Docker)**, затем **Shortener App** и открыть http://localhost:8080/swagger-ui.html.
5. Готовые запросы лежат в `http/links.http`: их можно отправлять прямо из редактора.

## Запуск без IDE

```bash
docker compose --profile app up --build   # база и приложение
```

Или только база в Docker, а приложение локально:

```bash
docker compose up -d
mvn spring-boot:run
```

## API

| Метод | Путь | Что делает |
|---|---|---|
| `POST` | `/api/links` | создать ссылку: `{"url": "...", "alias": "...", "ttlDays": 30}` (alias и ttlDays необязательны) |
| `GET` | `/{code}` | редирект на исходный адрес |
| `GET` | `/api/links/{code}` | информация о ссылке |
| `GET` | `/api/links/{code}/stats` | статистика переходов |
| `DELETE` | `/api/links/{code}` | удалить ссылку вместе со статистикой |

Пример:

```bash
curl -X POST localhost:8080/api/links -H 'Content-Type: application/json' \
     -d '{"url": "https://github.com/EfimovAN18", "alias": "my-github"}'
# {"code":"my-github","shortUrl":"http://localhost:8080/my-github", ...}

curl -i localhost:8080/my-github
# HTTP/1.1 302  Location: https://github.com/EfimovAN18
```

## Решения

- **302, а не 301.** Постоянный редирект браузер кэширует и больше не приходит к сервису, тогда переходы терялись бы для статистики.
- **Дата перехода хранится отдельной колонкой** в часовом поясе `Europe/Moscow` (настраивается). Группировка по дням становится обычным `GROUP BY` по индексу `(link_id, click_date)`.
- **Кэш вынесен в отдельный бин `LinkCache`.** Аннотации кэширования работают через прокси Spring и не срабатывают при вызове метода изнутри того же класса.
- **Уникальность кода гарантирует база** (`UNIQUE`), а сервис дополнительно проверяет её заранее, чтобы вернуть понятную ошибку `409`.
- **Схема управляется Flyway**, Hibernate работает в режиме `validate`.

## Структура

```
src/main/java/ru/efimov/shortener
├── config/   настройки и бины (часы, генератор кодов)
├── link/     сущность ссылки, репозиторий, кэш, сервис, DTO
├── click/    сущность перехода и агрегирующие запросы
├── web/      контроллеры и обработка ошибок
└── util/     генерация кодов, проверка адресов, разбор User-Agent и Referer
```

## Что можно добавить

- Redis вместо локального кэша, чтобы запускать несколько экземпляров;
- асинхронную запись переходов через очередь;
- пользователей и авторизацию (JWT), чтобы у каждого был свой список ссылок;
- ограничение частоты запросов (rate limiting);
- QR-код для ссылки.
