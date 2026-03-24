package dev.sorn.orc.api;

import java.nio.file.Path;

@FunctionalInterface
public interface ToolLoader {

    Tool load(Path path);

}
