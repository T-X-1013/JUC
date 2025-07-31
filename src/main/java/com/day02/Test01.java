package com.day02;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test01 {
    static int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 50000; i++) {
                counter++;
            }
        }, "t1");

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 50000; i++) {
                counter--;
            }
        }, "t2");

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        log.info("{}",counter);
    }
}
