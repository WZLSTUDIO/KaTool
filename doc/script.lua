-- 加锁
function lock()
    -- 如果Lock存在
    if redis.call("exists",KEYS[1]) ~= 0 then
        -- 如果不是自己的锁
        if redis.call("hexists",KEYS[1],ARGV[1]) == 0 then
            -- 不是自己的锁
            return redis.call("pttl",KEYS[1]);
        end
        -- 如果是自己的锁就记录次数
        redis.call("hincrby",KEYS[1],ARGV[1],1);
        -- 延期
        redis.call("pexpire",KEYS[1],ARGV[2]);
    else
        redis.call("hset",KEYS[1],ARGV[1],1);
        -- 设置默认延期
        redis.call("pexpire",KEYS[1],ARGV[2]);
    end
    -- 如果Lock不存在，那么就直接加上就可以了，hhh
    return nil;
end
-- 续期       呃呃呃，这里不用lua也可以
function destribe()
    if redis.call("hexists",KEYS[1],ARGV[1]) ~= 0 then
        redis.call("pexpire",KEYS[1],ARGV[2]);
    end
    return nil;
end
--解锁
function unlock()
--    解锁的逻辑和加锁相似
    -- 如果Lock存在
    if redis.call("exists",KEYS[1]) ~= 0 then
        -- 如果是自己的锁
        if redis.call("hexists",KEYS[1],ARGV[1]) ~= 0 then
            -- 如果是最后一层 直接delete
            if redis.call("hget",KEYS[1],ARGV[1]) == 0 then
                redis.call("del",KEYs[1]);
                a=0
            else
                a=redis.call("hincrby",KEYS[1],ARGV[1],-1);
            end
        end
        return a;
    end
    -- 如果Lock不存在，那么就return，hhh
    return nil;
end