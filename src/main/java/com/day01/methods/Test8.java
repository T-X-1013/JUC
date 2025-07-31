package com.day01.methods;

import lombok.extern.slf4j.Slf4j;

import static com.day01.utils.Sleeper.sleep;

@Slf4j(topic = "c.Test8")
public class Test8 {
    public static void main(String[] args) throws InterruptedException {
        Thread t2 = new Thread(()->{
            while(true) {
                Thread current = Thread.currentThread();
                boolean interrupted = current.isInterrupted();
                if(interrupted) {
                    log.info(" 打断状态: {}", interrupted);
                    break;
                }
            }
        }, "t2");
        t2.start();
        sleep(0.5);
        t2.interrupt();
    }
}