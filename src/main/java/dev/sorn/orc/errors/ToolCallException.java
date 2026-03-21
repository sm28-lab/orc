package dev.sorn.orc.errors;

public class ToolCallException extends OrcException {

    public ToolCallException(String message, Object... args) {
        super(message, args);
    }

}