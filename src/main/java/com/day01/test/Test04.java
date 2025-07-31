package com.day01.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.TestMultiThread")
public class Test04 {
    public static void main(String[] args) {
        new Thread(() -> {
            while(true) {
                log.info("running");
            }
        },"t1").start();
        new Thread(() -> {
            while(true) {
                log.info("running");
            }
        },"t2").start();
    }

}
