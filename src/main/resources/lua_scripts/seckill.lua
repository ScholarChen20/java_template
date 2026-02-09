-- check_and_lock_seats_v3.lua
local activityId = ARGV[1]
local userId = ARGV[2]
local orderId = ARGV[3]
local tempKey = ARGV[4]

-- 1. 防重
local userActivityKey = "seckill:user:" .. userId .. ":activity:" .. activityId
if redis.call("EXISTS", userActivityKey) == 1 then
    redis.call("DEL", tempKey)  -- 清理临时数据
    return 2
end

-- 2. 读取所有座位
local seatLines = redis.call("LRANGE", tempKey, 0, -1)
if #seatLines == 0 then
    redis.call("DEL", tempKey)
    return -1
end

local seatKeyPrefix = "seckill:seat:" .. activityId
local seats = {}

-- 3. 解析并校验
for _, line in ipairs(seatLines) do
    local zone, row_str, col_str = line:match("([^,]+),([^,]+),([^,]+)")
    if not zone or not row_str or not col_str then
        redis.log(redis.LOG_WARNING, "Parse failed: line=[" .. tostring(line) .. "]")
        redis.call("DEL", tempKey)
        return -1
    end

    -- ★★★ 关键：trim 并转换 ★★★
    local function trim(s)
        return s and s:gsub("^%s*(.-)%s*$", "%1") or ""
    end

    zone = trim(zone)
    row_str = trim(row_str)
    col_str = trim(col_str)

    local row = tonumber(row_str)
    local col = tonumber(col_str)

    if not row or not col then
        redis.log(redis.LOG_WARNING, "Invalid number: zone=[" .. tostring(zone) .. "], row_str=[" .. tostring(row_str) .. "], col_str=[" .. tostring(col_str) .. "], row=" .. tostring(row) .. ", col=" .. tostring(col))
        redis.call("DEL", tempKey)
        return -1
    end

    -- ★★★ 修复：将解析后的座位加入 seats 表 ★★★
    table.insert(seats, {
        zone = zone,
        row = row,
        col = col
    })
end

-- 4. 检查是否可售
for _, s in ipairs(seats) do
    local area_key = seatKeyPrefix .. ":" .. s.zone
    local seat_key = s.row .. "_" .. s.col
    local status = redis.call("HGET", area_key, seat_key)
    if status == "1" or status == "2" then
        redis.call("DEL", tempKey)
        return 1
    end
end

-- 5. 锁定座位
for _, s in ipairs(seats) do
    local area_key = seatKeyPrefix .. ":" .. s.zone
    local seat_key = s.row .. "_" .. s.col
    redis.call("HSET", area_key, seat_key, "1")
end

-- 6. 记录防重 + 清理临时 key
redis.call("SET", userActivityKey, orderId, "EX", 3600)
redis.call("DEL", tempKey)

return 0