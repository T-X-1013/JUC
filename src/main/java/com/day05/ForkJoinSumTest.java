package com.day05;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class ForkJoinSumTest {

    public static void main(String[] args) {
        ForkJoinPool pool = new ForkJoinPool(4);

        MyTask task = new MyTask(10);

        int result = pool.invoke(task);

        log.info("最终计算结果: {}", result);
    }

    // 计算 1 ~ n 的和
    static class MyTask extends RecursiveTask<Integer> {
        private final int n;

        public MyTask(int n) {
            this.n = n;
        }

        @Override
        protected Integer compute() {
            String threadName = Thread.currentThread().getName();
            log.info("线程 {} 正在处理: {}", threadName, this);

            if (n == 1) {
                log.info("线程 {}：基础情况 {}，返回 1", threadName, this);
                return 1;
            }

            MyTask subTask = new MyTask(n - 1);
            subTask.fork();

            int subResult = subTask.join();
            int result = n + subResult;

            log.info("线程 {}：计算 {} + {} = {}", threadName, n, subResult, result);
            return result;
        }

        @Override
        public String toString() {
            return "MyTask{" + "n=" + n + '}';
        }
    }
}
