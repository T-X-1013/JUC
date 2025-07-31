package com.day03;

import lombok.extern.slf4j.Slf4j;

import static com.day01.utils.Sleeper.sleep;

@Slf4j(topic = "c.Dead")
public class Dead {

    public static void main(String[] args) {
        Object A = new Object();
        Object B = new Object();
        Thread t1 = new Thread(() -> {
            synchronized (A) {
                log.info("lock A");
                sleep(1);
                synchronized (B) {
                    log.info("lock B");
                    log.info("操作...");
                }
            }
        }, "t1");
        Thread t2 = new Thread(() -> {
            synchronized (B) {
                log.info("lock B");
                sleep(0.5);
                synchronized (A) {
                    log.info("lock A");
                    log.info("操作...");
                }
            }
        }, "t2");
        t1.start();
        t2.start();

    }
}