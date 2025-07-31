package com.day01.methods;

import com.day01.utils.FileReader;
import com.day01.Constants;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test")
public class Test1 {
    public static void main(String[] args) {
        Thread t1 = new Thread("t1") {
            @Override
            public void run() {
                log.info(Thread.currentThread().getName());
            }
        };
        t1.start();
        log.info("do something...");

    }

}
