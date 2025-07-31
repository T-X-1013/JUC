package com.day01.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test02")
public class Test02 {
    public static void main(String[] args) {
        Runnable r1 = new Runnable() {
            public void run() {
                log.info("Hello World~");
            }
        };

        // Java8以后，也可以用使用lambda表达式
        Runnable r2 = () -> { log.info("Hello~"); };

        Thread t1 = new Thread(r1, "tao");
        Thread t2 = new Thread(r2, "wang");
        t1.start();
        t2.start();

        log.info("Man!");
    }
}
