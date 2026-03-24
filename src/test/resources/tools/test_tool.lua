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

toolInstance = TestTool:new()