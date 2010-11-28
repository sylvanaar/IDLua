while true do
  a = a..b
end

a = a / 0

b = b % 0

a,b = nil

self = b



function a()
  if a then
    b = b/0
  end
end