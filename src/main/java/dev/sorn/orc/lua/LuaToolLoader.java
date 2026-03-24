package dev.sorn.orc.lua;

import dev.sorn.orc.api.Tool;
import dev.sorn.orc.api.ToolLoader;
import org.luaj.vm2.Globals;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

import static dev.sorn.orc.lua.LuaJsonConverter.fromLua;
import static dev.sorn.orc.lua.LuaJsonConverter.toLua;
import static org.luaj.vm2.lib.jse.JsePlatform.standardGlobals;

public class LuaToolLoader implements ToolLoader {

    private final Globals globals;

    public LuaToolLoader() {
        this.globals = standardGlobals();
    }

    @Override
    public Tool load(Path path) {
        try (final var fis = new FileInputStream(path.toFile());
             final var reader = new InputStreamReader(fis)) {
            globals.load(reader, path.getFileName().toString()).call();
            final var luaInstance = globals.get("toolInstance");
            if (luaInstance.isnil()) {
                throw new RuntimeException("Lua script must define 'toolInstance'");
            }
            return request -> fromLua(luaInstance.get("execute").call(luaInstance, toLua(request)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Lua tool: " + path, e);
        }
    }

}