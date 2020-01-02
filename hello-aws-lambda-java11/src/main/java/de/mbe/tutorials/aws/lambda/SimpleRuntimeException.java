package de.mbe.tutorials.aws.lambda;

public class SimpleRuntimeException extends RuntimeException {

    public SimpleRuntimeException(String errorMessage) {
        super(errorMessage);
    }
}
