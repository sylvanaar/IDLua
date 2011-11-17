--
-- Created by IntelliJ IDEA.
-- User: jon
-- Date: 11/5/11
-- Time: 9:53 AM
-- To change this template use File | Settings | File Templates.
--

local file = {}

function file:close() end
function file:flush() end
function file:lines() end
function file:read() end
function file:seek() end
function file:setvbuf() end
function file:write() end


module "io"

stdin  = file
stdout = file
stderr = file

function open() return file end
function tmpfile() return file end

function close() end
function flush() end
function input() end
function lines() end
function output() end
function popen() end
function read() end
function type() end
function write() end




