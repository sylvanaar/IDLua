function assert(boolean) end
function collectgarbage() end
function dofile() end
function error() end
_G = {}
function getfenv(level) end
function getmetatable(object) end
function ipairs (t) end
function load(func, optChunkname) end
--loadfile = "loadfile ([filename])",
--loadstring = "loadstring (string [, chunkname])",
--next = "next (table [, index])",
--pairs = "pairs (t)",
--pcall = "pcall (f, arg1, ...)",
--print = "print (...)",
--rawequal = "rawequal (v1, v2)",
--rawget = "rawget (table, index)",
--rawset = "rawset (table, index, value)",
--select = "select (index, ...)",
--setfenv = "setfenv (f, table)",
--setmetatable = "setmetatable (table, metatable)",
--tonumber = "tonumber (e [, base])",
--tostring = "tostring (e)",
--type = "type (v)",
--unpack = "unpack (list [, i [, j]])",
--_VERSION = "(string)",
--xpcall = "xpcall (f, err)",
--module = "module (name [, ...])",
--require = "require (modname)",
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