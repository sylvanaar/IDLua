
-- Concatenation in a loop
while true do
    -- warning here
    a = a .. b

    -- no warning here
    c = a .. b
end

-- Divide by 0
a = a / 0

-- Mod by 0
b = b % 0

-- To few expressions
a, b, c, d, e, f = 1, 2

-- Global self
self = b

-- Function is last (cannot determine)
a, b, c = 1, b()

-- Function is first (unbalanced)
a, b, c = b(), 1

-- Accessing index 0
b = a[0]

-- Overwriting last element (NYI)
a[#a] = 1

