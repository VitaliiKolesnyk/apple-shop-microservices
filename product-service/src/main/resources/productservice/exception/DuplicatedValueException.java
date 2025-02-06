package org.productservice.exception;

public class DuplicatedValueException extends RuntimeException {

    public DuplicatedValueException(String message) {
        super(message);
    }
}

