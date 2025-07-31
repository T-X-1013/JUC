package com.day01.test;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test01")
public class Test01 {
    public static void main(String[] args) {
        Thread t = new Thread() {
            @Override
            public void run() {
                log.info("running");
            }
        };
        t.setName("taotao");
        t.start();

        log.info("running");
    }
}
