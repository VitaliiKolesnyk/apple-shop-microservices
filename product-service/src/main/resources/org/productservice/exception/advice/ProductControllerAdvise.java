package org.productservice.exception.advice;

import lombok.extern.slf4j.Slf4j;
import org.productservice.exception.DuplicatedValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ProductControllerAdvise {

    @ExceptionHandler(DuplicatedValueException.class)
    public ResponseEntity<Error> catchDuplicatedValueException(DuplicatedValueException e) {
        Error error = new Error(e.getMessage());

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Error> catchRuntimeException(Exception e) {
        Error error = new Error("Unexpected error occurred, please try again or contacts us");

        e.printStackTrace();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
