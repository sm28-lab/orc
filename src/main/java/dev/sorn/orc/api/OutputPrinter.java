package dev.sorn.orc.api;

import tools.jackson.databind.JsonNode;
import java.util.function.Consumer;

public interface OutputPrinter {

    void printResult(Result<JsonNode> result);

    void printInfo(String message);

    void printError(String message);

    Consumer<String> createSpinnerConsumer();

}
