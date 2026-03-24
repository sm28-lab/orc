package dev.sorn.orc.lua;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.writeString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LuaToolLoaderTest {

    private LuaToolLoader loader;

    @BeforeEach
    void setUp() {
        loader = new LuaToolLoader();
    }

    @Test
    void loads_tool_from_valid_lua_script() throws URISyntaxException {
        // GIVEN
        var path = Path.of(getClass()
            .getClassLoader()
            .getResource("tools/test_tool.lua")
            .toURI());

        // WHEN
        var tool = loader.load(path);

        // THEN
        assertThat(tool).isNotNull();
    }

    @Test
    void throws_exception_when_lua_script_missing_tool_instance() throws IOException {
        // GIVEN
        var tempFile = createTempFile("test_tool_missing_instance", ".lua");
        writeString(tempFile, """
            TestTool = {}
            TestTool.__index = TestTool
            
            function TestTool:new()
                local obj = {}
                setmetatable(obj, TestTool)
                return obj
            end
            
            function TestTool:execute(request)
                return '{"request":' .. request.data .. '}'
            end
            
            -- toolInstance = TestTool:new() <-- commented out toolInstance
            """);

        // WHEN / THEN
        assertThatThrownBy(() -> loader.load(tempFile))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("toolInstance");

        // CLEANUP
        deleteIfExists(tempFile);
    }

    @Test
    void throws_exception_when_lua_script_does_not_exist() {
        // GIVEN
        var path = Path.of("non_existent.lua");

        // WHEN / THEN
        assertThatThrownBy(() -> loader.load(path))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to load Lua tool: non_existent.lua");
    }

}