package dev.sorn.orc.module;

import java.util.Scanner;

public final class PromptResolver {

    private PromptResolver() {}

    public static String resolve(String[] args) {
        if (args.length > 0) {
            return String.join(" ", args);
        }
        System.out.print("Enter your prompt: ");
        try (var scanner = new Scanner(System.in)) {
            return scanner.nextLine();
        }
    }

}