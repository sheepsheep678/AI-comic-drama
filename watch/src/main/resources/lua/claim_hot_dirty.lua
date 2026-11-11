-- 原子领取待重算问题：从 dirty 集合移动到 processing 集合
-- KEYS[1] = dirty, KEYS[2] = processing
-- ARGV[1] = 最多领取数量
local dirty = KEYS[1]
local processing = KEYS[2]
local count = tonumber(ARGV[1])
local ids = redis.call('SMEMBERS', dirty)
local n = math.min(#ids, count)
local claimed = {}
for i = 1, n do
    redis.call('SMOVE', dirty, processing, ids[i])
    claimed[i] = ids[i]
end
return claimed
