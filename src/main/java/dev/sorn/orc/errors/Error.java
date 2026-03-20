package dev.sorn.orc.errors;

import static java.lang.String.format;

public class Error extends RuntimeException {

    public Error(Throwable t) {
        super(t);
    }

    public Error(String message, Object... args) {
        super(format(message, args));
    }

}
