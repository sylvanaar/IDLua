-- usage: lua listing.lua <sourcefile.lua>

local tmpfile = os.tmpname()

os.execute("luac -l -- \""..arg[1].."\" >"..tmpfile)

local listing_lines = { [0] = {} }
local last = 0

-- group the listing file line by the source line which genereated it
for line in io.lines(tmpfile) do 
    -- get the line number of the source which generated this listing line
    local num = line:match("[:%[](%d+)[%],]")

    num = tonumber(num)
    if num then
        last = num
        
        -- create buckets for each source line number
        listing_lines[num] = listing_lines[num] or {}

        -- insert the listing lines into the buckets 
        -- we need to preserve the ordering of the listing lines
        table.insert(listing_lines[num], line)        
    elseif line:match("^%d") then
        table.insert(listing_lines[last], line) 
    end 
end

local listing = io.stdout 

local num = 1

-- Write out the main chunk listing header
for _,v in ipairs(listing_lines[0]) do
    listing:write("--"..v.."\n")
end

-- combine the original source with the listing file opcodes
for source_line in io.lines(arg[1]) do 
    -- save the indention so our comments can be indented with the source
    local indent = "--"..source_line:match("^%s*")

    -- the listing lines have the opcodes for this source line
    local opcodes = listing_lines[num]
    
    -- if there are opcodes for this line write them out in order
    if opcodes then
        for i=1,#opcodes do
            local text = opcodes[i]:match("^[^%]]*%]%s*(.*)$")
        
            if text then
                listing:write(indent..text.."\n")
            else
                listing:write(indent..opcodes[i].."\n")
            end
        end
    end
    
    -- finally write the source line
    listing:write(source_line.."\n"..(opcodes and "\n" or ""))

    num = num + 1
end


os.execute("del "..tmpfile)
