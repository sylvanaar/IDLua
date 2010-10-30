local foo

local foo = foo




-- I am testing globals/locals

function a(b, c, d)
    a, b = c, d
    return self
end

local function a()
    a, b = c, d
    return self
end

local t = {}
function t:b(c, d)
    return self
end

local foo = foo

local a = function()
    return
    end

b.c = function()
    return
    end

a.b.c.d = function()
    return
    end


local a, b, c, d, e, f = 1, 2, 3, 4


for k, v in pairs(t) do
end

for i = 1, 10 do
end

local x1 = function()
    print("local disappear: " .. tostring(x1)) -- prints "nil"
    end


function a()

    print(a)
end


a = [[Multi
Line
String]]

a = "Hello"
a = [==[


        