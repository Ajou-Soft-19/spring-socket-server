package com.ajousw.spring.web.controller.advice;

import com.ajousw.spring.web.controller.json.ApiResponseJson;
import com.ajousw.spring.web.controller.json.ResponseStatusCode;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;


@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
    private static final String ERROR_MSG_KEY = "errMsg";

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public ApiResponseJson handleRuntimeException(RuntimeException e) {
        log.error("", e);
        return new ApiResponseJson(HttpStatus.INTERNAL_SERVER_ERROR, ResponseStatusCode.SERVER_ERROR,
                Map.of(ERROR_MSG_KEY, ControllerMessage.INTERNAL_SERVER_ERROR_MSG));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataAccessException.class)
    public ApiResponseJson handleDataAccessException(Exception e) {
        log.error("DB 오류 발생", e);
        return new ApiResponseJson(HttpStatus.INTERNAL_SERVER_ERROR, ResponseStatusCode.SERVER_ERROR,
                Map.of(ERROR_MSG_KEY, ControllerMessage.INTERNAL_SERVER_ERROR_MSG));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponseJson handleNoHandlerFoundException() {
        return new ApiResponseJson(HttpStatus.NOT_FOUND, ResponseStatusCode.URL_NOT_FOUND,
                Map.of(ERROR_MSG_KEY, ControllerMessage.WRONG_PATH_ERROR_MSG));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ApiResponseJson handleBadRequestException(Exception e) {
        return new ApiResponseJson(HttpStatus.BAD_REQUEST, ResponseStatusCode.WRONG_PARAMETER,
                Map.of(ERROR_MSG_KEY, e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, TypeMismatchException.class,
            HttpMessageNotReadableException.class})
    public ApiResponseJson handleBadRequestBody() {
        return new ApiResponseJson(HttpStatus.BAD_REQUEST, ResponseStatusCode.WRONG_PARAMETER,
                Map.of(ERROR_MSG_KEY, ControllerMessage.WRONG_REQUEST_ERROR_MSG));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(BadCredentialsException.class)
    public ApiResponseJson handleBadCredentialsException(Exception e) {
        return new ApiResponseJson(HttpStatus.UNAUTHORIZED, ResponseStatusCode.LOGIN_FAILED,
                Map.of(ERROR_MSG_KEY, e.getMessage()));
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponseJson handleMethodNotAllowedException() {
        return new ApiResponseJson(HttpStatus.METHOD_NOT_ALLOWED, 0, Map.of());
    }

}
