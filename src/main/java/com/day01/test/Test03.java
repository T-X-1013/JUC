package com.day01.test;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Slf4j(topic = "c.Test03")
public class Test03 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask = new FutureTask<>(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                log.info("running");
                Thread.sleep(1000);
                return 100;
            }
        });

        Thread thread = new Thread(futureTask, "tao");
        thread.start();

        log.info("{}:", futureTask.get());
    }
}
