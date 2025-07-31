package com.day03;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

import static com.day01.utils.Sleeper.sleep;

@Slf4j(topic = "c.Test2")
public class Test2 {
    static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {

        Thread t1 = new Thread(() -> {
            log.info("启动...");
            try {
                lock.lockInterruptibly(); // 上锁，这是可打断的。
                // 如果没有竞争，那么此方法就会获取lock对象锁
                // 如果有竞争，就进入到阻塞队列，也就是阻塞状态，可以被其他线程用interrupt方法打断，并抛出异常
            } catch (InterruptedException e) {
                e.printStackTrace();
                log.info("等锁的过程中被打断");
                return;
            }
            try {
                log.info("获得了锁");
            } finally {
                lock.unlock();
            }
        }, "t1");

        lock.lock(); // main线程上锁，此时 lock.lockInterruptibly  就阻塞了
        log.info("获得了锁");
        t1.start();
        try {
            sleep(1);
            t1.interrupt();
            log.info("执行打断");
        } finally {
            lock.unlock();
        }
    }
}
