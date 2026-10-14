package br.com.fiap.orderservice.ratelimit;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Sliding window: accepts at most {@code limit} calls in any window of one second.
 */
public class RateLimiter {

    private static final long WINDOW_NANOS = 1_000_000_000L;

    private final int limit;
    private final Deque<Long> acceptedAt = new ArrayDeque<>();

    public RateLimiter(int limit) {
        this.limit = limit;
    }

    public synchronized boolean tryAcquire() {
        long now = System.nanoTime();
        while (!acceptedAt.isEmpty() && now - acceptedAt.peekFirst() >= WINDOW_NANOS) {
            acceptedAt.pollFirst();
        }
        if (acceptedAt.size() >= limit) {
            return false;
        }
        acceptedAt.addLast(now);
        return true;
    }

    public int getLimit() {
        return limit;
    }

}
