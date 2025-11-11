package org.winterfell.slardar.starter.support;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * .令牌桶限流器 - 支持高并发场景
 * 使用原子操作和CAS机制实现无锁化，提升并发性能
 * </p>
 *
 * @author Alex
 * @since 2025/11/11
 */
public class SimpleRateLimiter {

    /**
     * -- GETTER --
     *  获取桶容量
     *
     * @return 桶容量
     */
    // 桶的容量（最多存储多少令牌）
    private final long capacity;

    /**
     * -- GETTER --
     *  获取令牌生成速率
     *
     * @return 每秒生成令牌数
     */
    // 令牌生成速率（每秒生成多少令牌）
    private final long tokensPerSecond;

    // 当前令牌数（使用AtomicLong保证线程安全）
    private final AtomicLong availableTokens;

    // 上次添加令牌的时间戳（纳秒）
    private final AtomicLong lastRefillTimestamp;

    // 用于同步令牌补充操作的锁
    private final ReentrantLock refillLock;


    public SimpleRateLimiter(long tokensPerSecond) {
        this(100, tokensPerSecond);
    }

    public SimpleRateLimiter(long capacity, long tokensPerSecond) {
        if (capacity <= 0 || tokensPerSecond <= 0) {
            throw new IllegalArgumentException("容量和速率必须大于0");
        }
        this.capacity = capacity;
        this.tokensPerSecond = tokensPerSecond;
        this.availableTokens = new AtomicLong(capacity);
        this.lastRefillTimestamp = new AtomicLong(System.nanoTime());
        this.refillLock = new ReentrantLock();
    }

    /**
     * 尝试获取1个令牌
     *
     * @return 是否成功获取
     */
    public boolean tryAcquire() {
        return tryAcquire(1);
    }

    /**
     * 尝试获取指定数量的令牌
     *
     * @param tokens 需要的令牌数
     * @return 是否成功获取
     */
    public boolean tryAcquire(long tokens) {
        if (tokens <= 0) {
            throw new IllegalArgumentException("令牌数必须大于0");
        }
        if (tokens > capacity) {
            return false;
        }
        // 先补充令牌
        refill();
        // 使用CAS循环尝试获取令牌
        while (true) {
            long current = availableTokens.get();
            // 令牌不足
            if (current < tokens) {
                return false;
            }
            // 尝试使用CAS更新令牌数
            long next = current - tokens;
            if (availableTokens.compareAndSet(current, next)) {
                return true;
            }
            // CAS失败，继续循环重试
        }
    }

    /**
     * 阻塞式获取1个令牌
     *
     * @throws InterruptedException 中断异常
     */
    public void acquire() throws InterruptedException {
        acquire(1);
    }

    /**
     * 阻塞式获取令牌，直到获取成功
     *
     * @param tokens 需要的令牌数
     * @throws InterruptedException 中断异常
     */
    public void acquire(long tokens) throws InterruptedException {
        if (tokens <= 0) {
            throw new IllegalArgumentException("令牌数必须大于0");
        }
        if (tokens > capacity) {
            throw new IllegalArgumentException("请求令牌数超过桶容量");
        }
        while (!tryAcquire(tokens)) {
            // 计算需要等待的时间
            long deficit = tokens - availableTokens.get();
            if (deficit > 0) {
                long waitTimeNanos = (deficit * 1_000_000_000L) / tokensPerSecond;
                Thread.sleep(waitTimeNanos / 1_000_000, (int) (waitTimeNanos % 1_000_000));
            }
        }
    }

    /**
     * 补充令牌到桶中
     */
    private void refill() {
        long now = System.nanoTime();
        long lastRefill = lastRefillTimestamp.get();

        // 如果距离上次补充时间太短，直接返回
        if (now <= lastRefill) {
            return;
        }

        // 使用锁来确保只有一个线程执行补充操作
        if (refillLock.tryLock()) {
            try {
                // 双重检查
                long currentRefill = lastRefillTimestamp.get();
                if (now <= currentRefill) {
                    return;
                }

                // 计算应该生成的令牌数
                long elapsedNanos = now - currentRefill;
                long tokensToAdd = (elapsedNanos * tokensPerSecond) / 1_000_000_000L;
                if (tokensToAdd > 0) {
                    // 使用CAS更新令牌数，确保不超过容量
                    while (true) {
                        long current = availableTokens.get();
                        long next = Math.min(capacity, current + tokensToAdd);

                        if (current == next || availableTokens.compareAndSet(current, next)) {
                            break;
                        }
                    }
                    // 更新最后补充时间
                    lastRefillTimestamp.set(now);
                }
            } finally {
                refillLock.unlock();
            }
        }
    }

    /**
     * 获取当前可用令牌数
     *
     * @return 当前令牌数
     */
    public long getAvailableTokens() {
        refill();
        return availableTokens.get();
    }

    public long getCapacity() {
        return capacity;
    }

    public long getTokensPerSecond() {
        return tokensPerSecond;
    }

    public AtomicLong getLastRefillTimestamp() {
        return lastRefillTimestamp;
    }
}