package dev.sorn.orc.api;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

public interface ReaderFactory {

    Reader create(Path path) throws IOException;

}
