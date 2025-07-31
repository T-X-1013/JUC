package com.day01.methods;

import lombok.extern.slf4j.Slf4j;

import static com.day01.utils.Sleeper.sleep;

@Slf4j(topic = "c.Test7")
public class Test7 {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(()->{
            sleep(1);
        }, "t1");
        t1.start();
        sleep(0.5);
        t1.interrupt();
        log.info(" 打断状态: {}", t1.isInterrupted());
    }
}
