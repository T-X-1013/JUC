package com.day01.methods;

import lombok.extern.slf4j.Slf4j;

import static com.day01.utils.Sleeper.sleep;

@Slf4j(topic = "c.Test11")
public class Test11 {
    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            while (true) {
                boolean interrupted = Thread.currentThread().isInterrupted();
                if (interrupted) {
                    break;
                }
            }
            log.info("子线程运行结束...");
        }, "t1");

        // 将t1线程设置为守护线程，setDaemon默认为false
        t1.setDaemon(true);
        t1.start();
        sleep(1);       // 主线程等待1秒
        log.info("主线程运行结束...");

    }
}
