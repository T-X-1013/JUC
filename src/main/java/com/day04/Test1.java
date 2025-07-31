package com.day04;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InterruptedIOException;

import static com.day01.utils.Sleeper.sleep;

@Slf4j
public class Test1 {
    static boolean run = true;

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            while (run) {
                System.out.println(run);
                // ....
            }
        });
        t.start();
        sleep(1);
        log.info("停止");
        run = false; // 线程t不会如预想的停下来
    }
}
