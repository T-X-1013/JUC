package com.day03;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test4 {
    private static final Logger log = LoggerFactory.getLogger(Test4.class);
    static ReentrantLock lock = new ReentrantLock();
    static Condition waitCigaretteQueue = lock.newCondition();
    static Condition waitBreakfastQueue = lock.newCondition();
    static volatile boolean hasCigrette = false;
    static volatile boolean hasBreakfast = false;

    public static void main(String[] args) {
        log.info("顶针在等早餐和锐克5...");

        new Thread(() -> {
            try {
                lock.lock();
                log.info("白姑娘，我阿妈每天早上五点起床给我电子蔫充电，充好了没？");
                while (!hasCigrette) {
                    log.info("可恶，还没充好电吗！？？！？！这一带有蛇妖！！！");
                    try {
                        waitCigaretteQueue.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.info("充电完毕，顶针等到了他的瑞克五~~");
            } finally {
                lock.unlock();
            }
        }).start();

        new Thread(() -> {
            try {
                lock.lock();
                log.info("不碍事的白姑娘，我早饭好了没？");
                while (!hasBreakfast) {
                    log.info("早饭还没好啊。。。");
                    try {
                        waitBreakfastQueue.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                log.info("等到了他的早餐");
            } finally {
                lock.unlock();
            }
        }).start();

        sleep(1);
        sendBreakfast();
        sleep(1);
        sendCigarette();
    }

    private static void sendCigarette() {
        lock.lock();
        try {
            log.info("充电结束！！！");
            hasCigrette = true;
            waitCigaretteQueue.signal();
        } finally {
            lock.unlock();
        }
    }

    private static void sendBreakfast() {
        lock.lock();
        try {
            log.info("送早餐来了");
            hasBreakfast = true;
            waitBreakfastQueue.signal();
        } finally {
            lock.unlock();
        }
    }

    private static void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}



