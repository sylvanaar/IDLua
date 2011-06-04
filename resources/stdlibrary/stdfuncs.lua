function assert(v, message) end
function collectgarbage(opt, arg) end
function dofile(filename) end
function error(message, level) end
function getfenv(f) end
function getmetatable(object) end
function ipairs (t) end
function load(func, optChunkname) end
function loadfile (filename) end
function loadstring (string, chunkname) end
function next(table, index) end
function pairs(t) end
function pcall (f, arg1, ...) end
function print (...) end
function rawequal (v1, v2) end
function rawget (table, index) end
function rawset (table, index, value) end
function select (index, ...) end
function setfenv (f, table) end
function setmetatable (table, metatable) end
function tonumber (e, base) end
function tostring (e) end
function type (v) end
function unpack (list , i , j) end
function xpcall (f, err) end
function module (name, ...) end
function require (modname) end

_VERSION = "string"
_G = {}
coroutine = {}
debug = {}
io = {}
math = {}
os = {}
package = {}
string = {}
table = {}
--file = {}


function coroutine.create() end
function coroutine.resume() end
function coroutine.running() end
function coroutine.status() end
function coroutine.wrap() end
function coroutine.yield() end
function debug.debug() end
function debug.getfenv() end
function debug.gethook() end
function debug.getinfo() end
function debug.getlocal() end
function debug.getmetatable() end
function debug.getregistry() end
function debug.getupvalue() end
function debug.setfenv() end
function debug.sethook() end
function debug.setlocal() end
function debug.setmetatable() end
function debug.setupvalue() end
function debug.traceback() end
function io.close() end
function io.flush() end
function io.input() end
function io.lines() end
function io.open() end
function io.output() end
function io.popen() end
function io.read() end
function io.tmpfile() end
function io.type() end
function io.write() end
io.stdin = true
io.stdout= true

function math.abs() end
function math.acos() end
function math.asin() end
function math.atan() end
function math.atan() end
function math.ceil() end
function math.cos() end
function math.cosh() end
function math.deg() end
function math.exp() end
function math.floor() end
function math.fmod() end
function math.frexp() end
function math.huge() end
function math.ldexp() end
function math.log() end
function math.log() end
function math.max() end
function math.min() end
function math.modf() end
math.pi = 3.1415
function math.pow() end
function math.rad() end
function math.random() end
function math.randomseed() end
function math.sin() end
function math.sinh() end
function math.sqrt() end
function math.tan() end
function math.tanh() end

function os.clock() end
function os.date() end
function os.difftime() end
function os.execute() end
function os.exit() end
function os.getenv() end
function os.remove() end
function os.rename() end
function os.setlocale() end
function os.time() end
function os.tmpname() end

-- Resolving these requires the "Enable Additional Completions" option in Settings|Lua
function file:close() end
function file:flush() end
function file:lines() end
function file:read() end
function file:seek() end
function file:setvbuf() end
function file:write() end

function package.cpath() end
function package.loaded() end
function package.loaders() end
function package.loadlib() end
function package.path() end
function package.preload() end
function package.seeall() end

function string.byte() end
function string.char() end
function string.dump() end
function string.find() end
function string.format() end
function string.gmatch() end
function string.gsub() end
function string.len() end
function string.lower() end
function string.match() end
function string.rep() end
function string.reverse() end
function string.sub() end
function string.upper() end

function table.concat() end
function table.insert() end
function table.maxn() end
function table.remove() end
function table.sort() end

