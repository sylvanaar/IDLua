----
-- RemDebug 2.0 Beta
-- Copyright Kepler Project 2005 (http://www.keplerproject.org/remdebug)
--

-- *** This copy exists for convenience in launching remotely debugged code and then connecting to
-- *** it from the LuaEclipse plugin. As such, this file must be perfectly in-sync with engine.lua
-- *** in the LuaEclipse plugin source.

local CLIENT_LAUNCH_MODE = CLIENT_LAUNCH_MODE

local doDebugPrint = false

io.stdout:setvbuf("no")

--pcall(require, "luarocks.require")

local socket = require"socket"
--local url = require"socket.url"
local lfs = require"lfs"
local debug = require"debug"

local _g      = _G
local cocreate, cowrap = coroutine.create, coroutine.wrap

--io.open('/tmp/debug.log', 'w'):close()

--print = function(...) local f = io.open('/tmp/debug.log', 'a+') table.foreachi({...}, function(k,v) f:write(tostring(v)) f:write'\t' end) f:write'\n' f:close() end

module("remdebug.engine", package.seeall)


_COPYRIGHT = "2006 - Kepler Project"
_DESCRIPTION = "Remote Debugger for the Lua programming language"
_VERSION = "2.0.0beta"


CMD_RUN       = 1
CMD_STEP_INTO = 2
CMD_STEP_OVER = 3
CMD_STEP_OUT  = 4
CMD_BREAK     = 5


local function dprint(...)
    if doDebugPrint then
        print(...)
    end
end

math.randomseed(math.floor(os.clock() * 1000000))
local main = "main"..math.random(1,65536)
local coro_debugger
local events = { BREAK = 1, WATCH = 2 }
local breakpoints = {}
local callstack= {}
local localsstack= {}
local watches = {}
local step_into = {}
local step_over = {}
local step_out = {}
local step_level = {[main]=0}
local stack_depth = {[main]=0}
local running_thread = main
local nil_value = {}

local controller_host = "localhost"
local controller_port = 8171

local stacktrace = ""
local errorCallstack = {}
local errorLocals = {}
local errorTable
local inDisplayThreadTerminatingError = false
--local breakpointPlugins = {}

--TODO: figure out why the require "socket.url" above doesn't work!
local url = {}
function url.escape(s)
    return string.gsub(s, "([^A-Za-z0-9_])", function(c)
        return string.format("%%%02x", string.byte(c))
    end)
end

--TODO: figure out why the require "socket.url" above doesn't work!
function url.unescape(s)
    local val = string.gsub(s, "%%(%x%x)", function(hex)
        return string.char(_g.tonumber(hex, 16))
    end)
    return string.gsub(val, "+", " ")
end

--function addBreakpointPlugin(plugin)
--    table.insert(breakpointPlugins, plugin)
--end

-- - Breakpoints

local function set_breakpoint(file, line)
    dprint("set-breakpoint:", file, line)
    breakpoints[file] = breakpoints[file] or {}
    breakpoints[file][line] = true  
end

local function remove_breakpoint(file, line)
    dprint("remove-breakpoint exists?", file, line, breakpoints[file] and breakpoints[file][line])
    if breakpoints[file] then
        breakpoints[file][line] = nil
    end
end

local function has_breakpoint(file, line)
    --dprint("has-breakpoint?", file, line, breakpoints[file] and breakpoints[file][line])
    return breakpoints[file] and breakpoints[file][line]
end

--local function exec_breakpoint_plugins(file, line, level)
--    for _, plugin in ipairs(breakpointPlugins) do
--        local currentCommand =
--            step_over and CMD_STEP_OVER or
--            step_into and CMD_STEP_INTO or
--            step_out and CMD_STEP_OUT or
--            CMD_RUN
--
--        local breakCommand = plugin:breakpoint(currentCommand, file, line, level+1)
--
--        if breakCommand then
--            step_over = false
--            step_into = false
--            step_out = false
--        end
--
--        if breakCommand == CMD_STEP_INTO then
--            local thread = coroutine.running() or "main"
--            step_level[thread] = stack_depth[thread] + 1
--            step_into = true
--        end
--    end
--end

-- - Variables

local function restore_vars(level, vars)
    level = level + 1
    if type(vars) ~= 'table' then return end
    local func = debug.getinfo(level, "f").func
    local i = 1
    local written_vars = {}
    while true do
        local name = debug.getlocal(level, i)
        if not name then break end
        debug.setlocal(level, i, vars[name])
        written_vars[name] = true
        i = i + 1
    end
    i = 1
    while true do
        local name = debug.getupvalue(func, i)
        if not name then break end
        if not written_vars[name] then
            debug.setupvalue(func, i, vars[name])
            written_vars[name] = true
        end
        i = i + 1
    end
end

local function capture_vars(level)
    level = level + 1
    local vars = {}
    local func = debug.getinfo(level, "f").func
    local i = 1
    while true do
        local name, value = debug.getupvalue(func, i)
        if not name then break end
        vars[name] = value
        i = i + 1
    end
    i = 1
    while true do
        local name, value = debug.getlocal(level, i)
        if not name then break end
        vars[name] = value
        i = i + 1
    end
    setmetatable(vars, { __index = getfenv(func), __newindex = getfenv(func) })
    return vars
end

-- - Dir Utils

local function break_dir(path) 
    local paths = {}
    path = string.gsub(path, "\\", "/")
    for w in string.gfind(path, "[^\/]+") do
        table.insert(paths, w)
    end
    return paths
end

local function merge_paths(path1, path2)
    dprint('merge_paths', path1, path2)
    local paths1 = break_dir(path1)
    local paths2 = break_dir(path2)
    for i, path in ipairs(paths2) do
        if path == ".." then
            table.remove(paths1, table.getn(paths1))
        elseif path ~= "." then
            table.insert(paths1, path)
        end
    end
    return table.concat(paths1, "/")
end

-- - Hook Engine

local function getFileName(f)
    if string.find(f, "@") == 1 then
        local file = string.sub(f, 2)
        if cwd and string.sub(file, 1, 1) ~= "/" then
            return cwd.."/"..file
        end
        return file
    else
        return nil
    end
end

local function wipeTable(table)
    for k, v in pairs(table) do table[k] = nil end
end

local function fill_callstack(callstackTable, localsTable, fromLevel, maxFrames)
    assert(callstackTable and localsTable)
    local level = fromLevel + 1
    dprint("fill_callstack (fromLevel,maxFrames):", fromLevel, maxFrames)
    wipeTable(callstackTable)
    wipeTable(localsTable)
    local level_info = debug.getinfo(level)
    local frameCount = 0
    while level_info and frameCount < maxFrames do
        local locals = {}
        if level_info.what ~= "tail" then
            locals = {["(*globals)"]=getfenv(level)}
            i = 1
            local key, value = debug.getlocal(level, i)
            while key do
                --dprint("fill_callstack local key, val", tostring(key), tostring(value))
                if value == nil then
                    value = nil_value end
                locals[key] = value
                i = i + 1
                key, value = debug.getlocal(level, i)
            end
            i = 1
            local func = level_info.func
            while true do
                local key, value = debug.getupvalue(func, i)
                if not key then break end
                --dprint("fill_callstack upvalue key, val", tostring(key), tostring(value))
                if value == nil then
                    value = nil_value end
                locals[key] = value
                i = i + 1
            end
            table.insert(callstackTable, level_info)
            table.insert(localsTable, locals)
            frameCount = frameCount + 1
            dprint('stack level', level, level_info.name, level_info.namewhat, level_info.source, level_info.short_src, level_info.currentline )
        end
        dprint("-------------------------------------------------", level)
        level = level + 1
        level_info = debug.getinfo(level)
    end
end

local function debug_hook(event, line, level, thread)
    --io.stdout:write("ü")
    level = level or 2
    local thread = thread or main
    running_thread = thread

    if event == "call" then
        stack_depth[running_thread] = stack_depth[running_thread] + 1
    elseif event == "return" or event == "tail return" then
        stack_depth[running_thread] = stack_depth[running_thread] - 1
    else
        local file = getFileName(debug.getinfo(level, "S").source)
        if not file  then
            dprint('--unknown file --', line, file, thread, level)
        end
        -- TODO: eliminate dependency on lfs
        --file = merge_paths(lfs.currentdir(), file)

        -- Watches
        --local vars = capture_vars(level)
        --table.foreach(watches, function (index, value)
        --    setfenv(value, vars)
        --    local status, res = pcall(value)
        --    if status and res then
        --        dprint'debugging..'
        --        dprint('-------------', status, res)
        --        -- TODO: send watch events
        --        coroutine.resume(coro_debugger, events.WATCH, vars, file, line, index)
        --        --restore_vars(level, vars)
        --    end
        --end)

        --exec_breakpoint_plugins(file, line, level+1)

        local doBreak = false
        if has_breakpoint(file, line) then
            dprint("has_breakpoint:", running_thread, stack_depth[running_thread], step_level[running_thread])
            doBreak = true
        elseif file and string.find(file, "remdebug/engine%.lua$") then
            -- Do not allow breaking/stepping into this file unless we're in the
            -- displayCoroutineTerminatingError function below
            if line == displayCoroutineTerminatingErrorFirstLine then
                dprint("In engine.lua displayCoroutineTerminatingError - breaking")
                if not errorLocals or not errorLocals[1] then
                    print("An error reached the top of a coroutine but is not a proper error callstack", file, line, event)
                end
                errorLocals[1].FATAL_ERROR = errorTable
                callstack = errorCallstack
                localsstack = errorLocals

                callstack.idOffset = #callstack
                localsstack.idOffset = #localsstack

                file = errorCallstack[1].source
                line = errorCallstack[1].currentline
                -- Cause the network coroutine to wait for a command from the debug client
                coroutine.resume(coro_debugger, events.BREAK, vars, file, line)
            end
        elseif step_into[running_thread] then
            dprint("step_into")
            doBreak = stack_depth[running_thread] >= step_level[running_thread]
        elseif step_over[running_thread] or step_out[running_thread] then
            dprint(step_over[running_thread] and "step_over:" or "step_out:", stack_depth[running_thread], step_level[running_thread])
            doBreak = stack_depth[running_thread] <= step_level[running_thread]
        end

        if doBreak then
            step_into[running_thread] = false
            step_over[running_thread] = false
            step_out[running_thread] = false
            dprint(">> resuming to..", file, line, running_thread)

            -- stack
            fill_callstack(callstack, localsstack, level, stack_depth[running_thread]-1)

            -- Cause the network coroutine to wait for a command from the debug client
            coroutine.resume(coro_debugger, events.BREAK, vars, file, line)
            --restore_vars(level, vars)
        end
    end
end

-- -- Operation Engine

local commands = {}

local SUCCESS = "200 OK\n"
local RUNNING = "210 Running\n"
local STEPPING = "211 Stepping\n"
local BAD_REQUEST = "400 Bad Request\n"
local EXPRESSION_ERROR_ = "401 Error in Expression "
local STACK_DUMP_ = "101 Stack "
local VARIABLE_DUMP_ = "102 Variable "
local BREAK_PAUSE_ = "202 Paused "
local WATCH_PAUSE_ = "203 Paused "
local EXECUTION_ERROR_ = "401 Error in Execution "


local eventSink

function commands.createEventSocket(server, port)
    eventSink = socket.connect(controller_host, port)
    if eventSink then
        return SUCCESS
    else
        return EXECUTION_ERROR_
    end
end

--- implements SETB command
function commands.setBreakpoint(server, filename, lineNumber )
    if filename and lineNumber then
        filename = url.unescape(filename)
        dprint('breakpoint filename:', filename, lineNumber)
        set_breakpoint(filename, tonumber(lineNumber))

        eventSink:send(SUCCESS) -- TODO: create specific event?

        return SUCCESS 
    else
        dprint'no parameters'
        return BAD_REQUEST
    end
end

--- implements DELB command
function commands.removeBreakpoint(server, filename, lineNumber )
    if filename and lineNumber then
        filename = url.unescape(filename)
        dprint('remove-breakpoint filename, line:', filename, lineNumber)
        remove_breakpoint(filename, tonumber(lineNumber))

        eventSink:send(SUCCESS) -- TODO: create specific event?

        return SUCCESS 
    else
        return BAD_REQUEST
    end
end

function commands.execute(server, status, chunk)
    if chunk then 
        local func = loadstring(chunk)
        local status, res
        if func then
            setfenv(func, eval_env)
            status, res = xpcall(func, debug.traceback)
        end
        res = tostring(res)
        if status then
            local s = SUCCESS .. " " .. string.len(res) .. "\n" 

            eventSink:send(s) 

            return s .. res
        else
            local s = EXPRESSION_ERROR_ .. string.len(res) .. "\n"
            return s .. res
        end
    else
        return BAD_REQUEST
    end
end

function commands.setWatch(server, status, exp)
    if exp then 
        local func = loadstring("return(" .. exp .. ")")
        local newidx = table.getn(watches) + 1
        watches[newidx] = func
        table.setn(watches, newidx)

        local s = SUCCESS .. " " .. newidx .. "\n" 

        eventSink:send(s) 

        return s
    else
        return BAD_REQUEST
    end  
end

function commands.deleteWatch(server, status, index)
    index = tonumber(index)
    if index then
        watches[index] = nil
        eventSink:send(SUCCESS) 
        return SUCCESS 
    else
        return BAD_REQUEST
    end
end

local function getRunResponseString(event, file, line, idx_watch)
    local s
    if event == events.BREAK then
        s = BREAK_PAUSE_ .. file .. " " .. line .. "\n"
    elseif event == events.WATCH then
        s = WATCH_PAUSE_ .. file .. " " .. line .. " " .. idx_watch .. "\n"
    else
        s = EXECUTION_ERROR_ .. string.len(file) .. "\n" .. file
    end
    return s
end

function commands.run(server)
    dprint'run success'
    eventSink:send(RUNNING) 

    -- Yield back to the debug hook coroutine
    local ev, vars, file, line, idx_watch = coroutine.yield()

    eval_env = vars

    local s = getRunResponseString(ev, file, line, idx_watch)
    eventSink:send(s) 

    return s
end

function commands.step(server)
    dprint'step success' dprint(SUCCESS)
    eventSink:send(STEPPING) 
    step_into[running_thread] = true
    step_level[running_thread] = stack_depth[running_thread] + 1
    -- Yield back to the debug hook coroutine
    local ev, vars, file, line, idx_watch = coroutine.yield()

    eval_env = vars

    local s = getRunResponseString(ev, file, line, idx_watch)
    eventSink:send(s) 

    return s
end

function commands.stepOver(server)
    dprint'stepover success'
    eventSink:send(STEPPING) 

    step_over[running_thread] = true
    step_level[running_thread] = stack_depth[running_thread]
    -- Yield back to the debug hook coroutine
    local ev, vars, file, line, idx_watch = coroutine.yield()

    eval_env = vars

    local s = getRunResponseString(ev, file, line, idx_watch)
    eventSink:send(s) 

    return s
end

function commands.stepOut(server)
    dprint'stepout success'
    eventSink:send(STEPPING) 

    step_out[running_thread] = true
    step_level[running_thread] = stack_depth[running_thread] - 1
    -- Yield back to the debug hook coroutine
    local ev, vars, file, line, idx_watch = coroutine.yield()

    eval_env = vars

    local s = getRunResponseString(ev, file, line, idx_watch)
    eventSink:send(s) 

    return s
end

function commands.variable(server, stackLevel, variableName)
    dprint'commands.variable success'

    local idOffset = localsstack.idOffset or 0

    local locals = localsstack[tonumber(stackLevel)-idOffset] or {}

    --table.foreach(localsstack, dprint)
    --dprint(locals, #locals, variableName)
    --table.foreach(locals, dprint)
    --dprint(locals[variableName])
    local item = locals
    local firstItem = true
    local firstVariableName, lastVariableName
    local variableNameDepth = 0
    string.gsub(variableName, "([^:]+)", function(o)
        if item then
            o = url.unescape(o)
            if firstItem then
                firstVariableName = o
                firstItem = false
            end
            lastVariableName = o
            local val = item[o] or tonumber(o) and item[tonumber(o)] 
            if val == nil and variableNameDepth > 0 then
                for k,v in pairs(item) do
                    if o == tostring(k) then
                        val = v
                        break
                    end
                end
            end
            item = val
            variableNameDepth = variableNameDepth + 1
        end
    end)
    local value = item
    local value_string = tostring(value)

    --print("command.variable:", tostring(value), type(value), variableName, variableNameDepth, firstVariableName, lastVariableName)
    if inDisplayThreadTerminatingError and variableNameDepth == 1 and firstVariableName == "FATAL_ERROR" then
        value_string = "A Fatal Error Reached the Top of the Interpreter or Coroutine"
    end

    local s = url.escape(value_string)
    xpcall(function()
        if type(value)=='table' then
            s = s .. "#"
            for key, val in pairs(value) do
                s = s .. url.escape(tostring(key)) .. '=' .. url.escape(type(val)) .. "|"
            end
        end
        s = s .. '\n'
        --dprint("returning variable:", VARIABLE_DUMP_ .. stackLevel .. " " .. variableName .. s)
        eventSink:send(VARIABLE_DUMP_ .. stackLevel .. " " .. variableName .. s) 
    end, dprint)

    return s

end

function commands.stack(server)
    dprint'commands.stack success'
    local stack = {}
    local i=stack_depth[running_thread]
    dprint('beginning at ' .. i)
    --local __ = function(s) return string.gsub(tostring(s or ''), '([|#])', '\\%1') end -- escaped control characters
    local __ = function(s) return url.escape(tostring(s or '')) end -- escaped control characters
    local _ = function(s) return __(s) .. '|' end 

    local join = function(t) 
        local tt = {}
        for key, value in pairs(t) do 
            if value == nil_value then
                table.insert(tt, __(key) .. "=" .. url.escape("nil"))
            else
                table.insert(tt, __(key) .. "=" .. url.escape(type(value)) )
            end
        end 
        return table.concat(tt, '|') 
    end

    local idOffset = callstack.idOffset or 0

    for i, aa in ipairs(callstack) do
        if aa then
            local locals = localsstack[i]
            table.insert(stack, _(i+idOffset) .. _(aa.name) .. _(aa.namewhat) .. _(getFileName(aa.source)) .. _(aa.short_src) .. _( aa.currentline ) .. join(locals))
        end
    end
    local s = STACK_DUMP_ .. table.concat(stack, '#') .. '\n'
    dprint("returning stack message:", s)

    eventSink:send(s) 

    return s
end


function commands.exit(server)
    dprint'commands.exit success'
    if eventSink then 
        eventSink:close() 
    end
    if server then 
        server:close() 
    end
    os.exit(1)
end


local function grabStacktrace(message)
    stacktrace = debug.traceback(message, 2)
    dprint("STACKTRACE:", stacktrace)
    local thread = coroutine.running() or main
    fill_callstack(errorCallstack, errorLocals, 2, stack_depth[thread]-2)
    return message
end

-- -- Debugger Loop

local function debugger_loop(server)
    local command
    local eval_env = {}

    -- command x operations table.. allows alternate syntaxes to commands
    local operations = {
        SUBSCRIBE={
            operation = 'createEventSocket',
            paramsMask = '(%d+)$'
        },
        SETB={
            operation = 'setBreakpoint',
            paramsMask = '([%w%p]+)%s+(%d+)$'
        },
        DELB={
            operation = 'removeBreakpoint', 
            paramsMask = '([%w%p]+)%s+(%d+)$'
        },
        EXEC={
            operation = 'execute', 
            paramsMask = '.*'
        },
        SETW={
            operation = 'setWatch', 
            paramsMask = '.*'
        },
        DELW={
            operation = 'deleteWatch', 
            paramsMask = '(%d+)'
        },
        RUN ={
            operation = 'run', 
            paramsMask = '.*'
        },
        RESUME ={
            operation = 'run', 
            paramsMask = '.*'
        },
        EXIT ={
            operation = 'exit', 
            paramsMask = '.*'
        },
        STACK ={
            operation = 'stack', 
            paramsMask = '.*'
        },
        EXAMINE ={
            operation = 'variable', 
            paramsMask = '(%d+) (.+)'
        },
        STEP={
            operation = 'step', 
            paramsMask = '.*'
        },
        OVER={
            operation = 'stepOver', 
            paramsMask = '.*'
        },
        OUT={
            operation = 'stepOut', 
            paramsMask = '.*'
        },
    }

    while true do
        dprint"I'm about to receive from server"
        local line, status = server:receive()
        dprint('received', "'" .. tostring(line) .. "'", ">" .. tostring(status).. "<")
        local result  = BAD_REQUEST

        for command, params in string.gmatch(line, "([A-Z]+)%s*(.*)") do
            dprint('found:', command, params)
            dprint('executing command:',command, params)
            local operation = operations[command] and operations[command].operation
            dprint('executing...', tostring(command), tostring(operation))
            if operation then
                dprint('about to run', tostring(operation), tostring(commands[operation]), tostring(params),  operations[command].paramsMask)
                local outparams = {}
                string.gsub(params, operations[command].paramsMask, function(...)
                    outparams = {...}
                end)
                dprint'parameters'
                table.foreach(outparams, dprint)
                local success
                local function callFunc()
                    return commands[operation](server, unpack(outparams))
                end
                success, result = xpcall(callFunc, grabStacktrace)
                if not success then
                    print("Error in debug loop:", operation, result, stacktrace)
                end

                dprint('r',result)
            end
        end
        --dprint('R',result)
        server:send(result)
    end  
    dprint("ENDING DEBUGGER LOOP")
end

-- -- Creating the Coroutine Debugger -- -- 

coro_debugger = coroutine.create(debugger_loop)


-- -- Public Interface -- -- 

--
-- remdebug.engine.config(tab)
-- Configures the engine
--
function config(tab)
    if tab.host then
        controller_host = tab.host
    end
    if tab.port then
        controller_port = tab.port
    end
end

local function setHook(hookFunc)
    -- We have to set this here so that when this function returns debug_hook has a valid value to
    -- decrement
    local thread = coroutine.running() or main
    stack_depth[thread] = 1
    step_level [thread] = 0
    debug.sethook(hookFunc, "lcr")

    -- This block causes erratic behavior in the debugger, but it works really well to temporarily
    -- identify an error originating in the hook function
    --local function newHookFunc(...)
    --    local success, ret = pcall(hookFunc, ...)
    --    if not success then
    --        print("Error in hook function:", ret)
    --    end
    --    return ret
    --end
    --debug.sethook(newHookFunc, "lcr")
end

--
-- remdebug.engine.start()
-- Tries to start the debug session by connecting with a controller
--
function start()
    pcall(require, "remdebug.config")
    dprint('Connecting...', controller_host, controller_port)
    for i = 1,60 do
        if i % 3 == 0 then
            print("Connecting to debug client...", controller_host, controller_port)
        end
        local server = socket.connect(controller_host, controller_port)
        if server then
            dprint'Success!'
            setHook(debug_hook)
            local thread = coroutine.running() or main
            stack_depth[thread] = 0
            step_level [thread] = 0
            -- Launch the network coroutine that waits for a command from the debug client 
            local result = coroutine.resume(coro_debugger, server)
            return result
        end
        socket.sleep(1)
    end
    error('Could not connect to server: ' .. (msg or ''))
end

function launch(luaFile)
    local thread = coroutine.running() or main
    local function callFunc()
        stack_depth[thread] = 0
        step_level [thread] = 0
        dofile(luaFile)
    end
    inDisplayThreadTerminatingError = false
    local success, ret = xpcall(callFunc, grabStacktrace)
    if not success then
        local success, globalError = pcall(function() return getLastUncaughtError() end)
        inDisplayThreadTerminatingError = true
        displayCoroutineTerminatingError(thread, ret, stacktrace, success and globalError)
        error(ret)
    end
end

local function getCoroutineFunction(f, hook, ...)
    return function(...)
        local thread = coroutine.running() or main
        dprint("getCoroutineFunction thread, hook", thread, hook)
        local function thread_hook(event,line)
            hook(event, line, 3, thread)
        end
        setHook(thread_hook)
        local args = arg
        local function callFunc()
            return f(unpack(args))
        end
        inDisplayThreadTerminatingError = false
        local success, ret = xpcall(callFunc, grabStacktrace)
        if not success then
            local success, globalError = pcall(function() return getLastUncaughtError() end)
            inDisplayThreadTerminatingError = true
            displayCoroutineTerminatingError(thread, ret, stacktrace, success and globalError)
            error(ret)
        end
        return ret
    end
end

--
--This function overrides the built-in for the purposes of propagating
--the debug hook settings from the creator into the created coroutine.
--
_g.coroutine.create = function(f)
    dprint("coroutine.create!", f)
    return cocreate(getCoroutineFunction(f, debug_hook))
end

--
--This function overrides the built-in for the purposes of propagating
--the debug hook settings from the creator into the created coroutine.
--
_g.coroutine.wrap = function(f)
    dprint("coroutine.wrap!", f)
    return cowrap(getCoroutineFunction(f, debug_hook))
end

displayCoroutineTerminatingErrorFirstLine = debug.getinfo(1, 'l').currentline + 5
function displayCoroutineTerminatingError(coroutine, errorMessage, stacktrace, errorObject)
    --If your debugger has stopped on this line, an error bubbled up to the top of a coroutine and
    --has terminated that coroutine
    errorTable = {coroutine=coroutine, errorMessage=errorMessage, stacktrace=stacktrace, errorObject=errorObject}
    print("Error:", tostring(coroutine), tostring(errorMessage), tostring(stacktrace), errorObject and errorObject:toString() or "")
end

if not CLIENT_LAUNCH_MODE then
    if not launchCount then
        launchCount = 1
        cwd = lfs.currentdir()
        start()
        local thread = coroutine.running() or main
        stack_depth[thread] = 4
        step_level [thread] = 0
    end
end
