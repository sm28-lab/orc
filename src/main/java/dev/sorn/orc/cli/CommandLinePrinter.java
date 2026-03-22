package dev.sorn.orc.cli;

import dev.sorn.orc.api.OutputPrinter;
import dev.sorn.orc.api.Result;
import dev.sorn.orc.json.Json;
import tools.jackson.databind.JsonNode;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class CommandLinePrinter implements OutputPrinter {

    public void printResult(Result<JsonNode> result) {
        result.fold(
            val -> {
                System.out.println("\n--- Result ---");
                System.out.println(Json.toJson(val));
                return val;
            },
            err -> {
                System.err.println("\nError: " + err.getMessage());
                return null;
            }
        );
    }

    @Override
    public void printInfo(String message) {
        System.out.println(message);
    }

    @Override
    public void printError(String message) {
        System.err.println(message);
    }

    @Override
    public Consumer<String> createSpinnerConsumer() {
        final var spinner = new char[]{'⠋', '⠙', '⠹', '⠸', '⠼', '⠴', '⠦', '⠧'};
        final var idx = new AtomicInteger(0);
        return message -> {
            System.out.print("\r\033[2K");
            final var frame = spinner[idx.getAndIncrement() % spinner.length];
            System.out.print("[" + frame + "] " + message);
            System.out.flush();
        };
    }

}
