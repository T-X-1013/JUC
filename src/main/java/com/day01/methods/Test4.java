package com.day01.methods;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test4")
public class Test4 {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.info("enter sleep...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    log.info("wake up...");
                    e.printStackTrace();
                }
            }
        };
        t1.start();

        Thread.sleep(1000);
        log.info("interrupt...");
        t1.interrupt();

    }
}
