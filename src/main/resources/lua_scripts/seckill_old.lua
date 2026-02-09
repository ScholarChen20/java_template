-- 1.参数列表
-- 1.1 活动id
local activityId = ARGV[1]
-- 1.2 座位区对牌号表 VIP 1_1 表示VIP区的1排1号
local seatCodes = ARGV[2]
-- 1.3 用户id
local userId = ARGV[3]
-- 1.4 订单id
local orderId = ARGV[4]

-- 2.定义key
local seatKeyPrefix = "seckill:seat:" .. activityId
local orderKey = "seckill:order:" .. orderId

-- 3.执行脚本
-- 3.1 判断座位号的状态
for k,v in pairs(seatCodes) do
    local area_key = seatKeyPrefix .. ":" .. v
    local seat_key = row .. "_" .. col

    -- 获取座位状态
    local status = redis.call("hget", area_key, seat_key)

    if not status then
        status = "0"
    end

    if(status == "1" or status == "2") then
        -- 座位已锁定或售出
        return 1
    end

end

-- 3.3 判断用户是否重复下单
if(redis.call("sismember", orderKey, userId) == 1) then
    -- 用户重复下单
    return 2
end

-- 4.1 修改座位状态-变为锁定状态
for k,v in pairs(seatCodes) do
    local area_key = seatKeyPrefix .. ":" .. v
    local seat_key = row .. "_" .. col
    redis.call("hset", area_key, seat_key, "1")
end

-- 4.2 记录订单
redis.call("sadd", orderKey, userId)

return 0 -- 秒杀成功