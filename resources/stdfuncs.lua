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
_VERSION = "string"
function xpcall (f, err) end
function module (name, ...) end
function require (modname) end

_G = {}
coroutine = {}
debug = {}
io = {}
math = {}
os = {}
package = {}
string = {}
table = {}

function io.write() end

math.sqrt = function(val) end

math.pi = 3.1415