package com.example.newsfetcher.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisLockService {

    private final RedissonClient client;

    public RedisLockService(RedissonClient client) {
        this.client = client;
    }

    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        RLock lock = client.getLock(key);
        return lock.tryLock(waitTime, leaseTime, unit);
    }

    public void unlock(String key) {
        RLock lock = client.getLock(key);
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (IllegalMonitorStateException ignored) {
        }
    }
}
