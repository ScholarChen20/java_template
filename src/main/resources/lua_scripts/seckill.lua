---
--- Created by Administrator.
--- DateTime: 2026/2/10 15:22
---

-- check_and_lock_seats_final.lua
local activityId = ARGV[1]
local seatCount = tonumber(ARGV[2])
local userId = ARGV[3 + seatCount * 3]
local orderId = ARGV[4 + seatCount * 3]

-- 1. 用户防重（按活动）
local userActivityKey = "seckill:user:" .. userId .. ":activity:" .. activityId
if redis.call("EXISTS", userActivityKey) == 1 then
    return 2  -- 重复下单
end

-- 2. 校验所有座位
local baseKey = "ticket:seat:stock:" .. activityId
for i = 0, seatCount - 1 do
    local idx = 3 + i * 3
    local zone = ARGV[idx]
    local row = tonumber(ARGV[idx + 1])
    local col = tonumber(ARGV[idx + 2])

    if not zone or not row or not col then
        return -1  -- 参数错误
    end

    -- 检查座位状态
    local status = redis.call("HGET", baseKey .. ":" .. zone, row .. "_" .. col)
    if not status or status ~= "0" then
        -- 座位不存在、已锁定或已售出
        return 1
    end
end

-- 3. 原子锁定所有座位
for i = 0, seatCount - 1 do
    local idx = 3 + i * 3
    local zone = ARGV[idx]
    local row = ARGV[idx + 1]
    local col = ARGV[idx + 2]
    redis.call("HSET", baseKey .. ":" .. zone, row .. "_" .. col, "1")
end

-- 4. 记录防重
redis.call("SET", userActivityKey, orderId, "EX", 3600)

return 0  -- 成功
