package com.day01.methods;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test2")
public class Test2 {

    public static void main(String[] args) {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.debug("running...");
            }
        };

        System.out.println(t1.getState());
        t1.start();
        System.out.println(t1.getState());
    }
}