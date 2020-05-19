package com.netflix.discovery.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RateLimiter 目前支持分钟级和秒级两种速率限制
 * Rate limiter implementation is based on token bucket algorithm. There are two parameters:
 * <ul>
 * <li>
 *     burst size - maximum number of requests allowed into the system as a burst
 * </li>
 * <li>
 *     average rate - expected number of requests per second (RateLimiters using MINUTES is also supported)
 * </li>
 * </ul>
 *
 * @author Tomasz Bak
 */
public class RateLimiter {

    /**
     * 速率单位转换成毫秒
     */
    private final long rateToMsConversion;
    //消耗令牌数
    private final AtomicInteger consumedTokens = new AtomicInteger();
    //最后填充令牌的时间
    private final AtomicLong lastRefillTime = new AtomicLong(0);

    @Deprecated
    public RateLimiter() {
        this(TimeUnit.SECONDS);
    }



    //averageRateUnit 参数，速率单位。构造方法里将 averageRateUnit 转换成 rateToMsConversion
    public RateLimiter(TimeUnit averageRateUnit) {
        switch (averageRateUnit) {
            case SECONDS: // 秒级
                rateToMsConversion = 1000;
                break;
            case MINUTES: // 分钟级
                rateToMsConversion = 60 * 1000;
                break;
            default:
                throw new IllegalArgumentException("TimeUnit of " + averageRateUnit + " is not supported");
        }
    }

    //获取令牌，并返回是否获取成功

    /**
     * 获取令牌( Token )
     * @param burstSize 令牌桶上限
     * @param averageRate 令牌再装平均速率
     * @return 是否获取成功
     */
    public boolean acquire(int burstSize, long averageRate) {
        return acquire(burstSize, averageRate, System.currentTimeMillis());
    }

    public boolean acquire(int burstSize, long averageRate, long currentTimeMillis) {
        if (burstSize <= 0 || averageRate <= 0) { // Instead of throwing exception, we just let all the traffic go
            return true;
        }

        // 填充 令牌
        refillToken(burstSize, averageRate, currentTimeMillis);
        // 消费 令牌
        return consumeToken(burstSize);
    }

    //填充已消耗的令牌
    private void refillToken(int burstSize, long averageRate, long currentTimeMillis) {
        //// 获得 最后填充令牌的时间每次填充令牌，会设置 currentTimeMillis 到 refillTime
        long refillTime = lastRefillTime.get();
        // // 获得 过去多少毫秒 用于计算需要填充的令牌数
        long timeDelta = currentTimeMillis - refillTime;
        // 计算 可填充最大令牌数量 newTokens 可能超过 burstSize ，所以下面会有逻辑调整newTokens
        long newTokens = timeDelta * averageRate / rateToMsConversion;
        if (newTokens > 0) {
            // 计算 新的填充令牌的时间  算新的填充令牌的时间。为什么不能用 currentTimeMillis 呢？例如，averageRate = 500 && averageRateUnit = SECONDS 时，
            // 每 2 毫秒才填充一个令牌，如果设置 currentTimeMillis ，会导致不足以填充一个令牌的时长被吞了
            long newRefillTime = refillTime == 0
                    ? currentTimeMillis
                    : refillTime + newTokens * rateToMsConversion / averageRate;
            // CAS 保证有且仅有一个线程进入填充
            if (lastRefillTime.compareAndSet(refillTime, newRefillTime)) {
                while (true) {// 死循环，直到成功
                    // 计算 填充令牌后的已消耗令牌数量
                    int currentLevel = consumedTokens.get();
                    //burstSize 可能调小，例如，系统接入分布式配置中心，可以远程调整该数值。如果此时 burstSize 更小，以它作为已消耗的令牌数量
                    int adjustedLevel = Math.min(currentLevel, burstSize); // In case burstSize decreased
                    int newLevel = (int) Math.max(0, adjustedLevel - newTokens);
                    // CAS 避免和正在消费令牌的线程冲突
                    if (consumedTokens.compareAndSet(currentLevel, newLevel)) {
                        return;
                    }
                }
            }
        }
    }

    //填充**消耗( 获取 )**的令牌
    private boolean consumeToken(int burstSize) {
        while (true) { // 死循环，直到没有令牌，或者获取令牌成功
            // 没有令牌
            int currentLevel = consumedTokens.get();
            if (currentLevel >= burstSize) {
                return false;
            }
            // CAS 避免和正在消费令牌或者填充令牌的线程冲突
            if (consumedTokens.compareAndSet(currentLevel, currentLevel + 1)) {
                return true;
            }
        }
    }

    public void reset() {
        consumedTokens.set(0);
        lastRefillTime.set(0);
    }
}
