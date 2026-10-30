package ru.efimov.shortener.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.efimov.shortener.link.LinkExceptions;

/**
 * Переводит исключения предметной области в ответы формата RFC 9457 (Problem Details).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(LinkExceptions.NotFound.class)
    public ProblemDetail notFound(LinkExceptions.NotFound e) {
        return problem(HttpStatus.NOT_FOUND, "Ссылка не найдена", e.getMessage());
    }

    @ExceptionHandler(LinkExceptions.Expired.class)
    public ProblemDetail expired(LinkExceptions.Expired e) {
        return problem(HttpStatus.GONE, "Ссылка устарела", e.getMessage());
    }

    @ExceptionHandler(LinkExceptions.AliasTaken.class)
    public ProblemDetail aliasTaken(LinkExceptions.AliasTaken e) {
        return problem(HttpStatus.CONFLICT, "Алиас занят", e.getMessage());
    }

    @ExceptionHandler(LinkExceptions.BadRequest.class)
    public ProblemDetail badRequest(LinkExceptions.BadRequest e) {
        return problem(HttpStatus.BAD_REQUEST, "Некорректный запрос", e.getMessage());
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
