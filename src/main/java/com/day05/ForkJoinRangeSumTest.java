package com.day05;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class ForkJoinRangeSumTest {

    public static void main(String[] args) {
        // 创建线程池，最多并行 4 个线程
        ForkJoinPool pool = new ForkJoinPool(4);

        // 创建计算任务：求 1 到 5 的和
        AddTask task = new AddTask(1, 10);

        // 执行任务并获取结果
        int result = pool.invoke(task);

        log.info("最终计算结果: {}", result); // 应该输出 15
    }

    static class AddTask extends RecursiveTask<Integer> {
        int begin;
        int end;

        public AddTask(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public String toString() {
            return "{" + begin + "," + end + '}';
        }

        @Override
        protected Integer compute() {
            String threadName = Thread.currentThread().getName();
            log.info("线程 {} 正在计算: {}", threadName, this);

            if (begin == end) {
                log.info("线程 {}：基础情况 {}，返回 {}", threadName, this, begin);
                return begin;
            }

            if (end - begin == 1) {
                int sum = begin + end;
                log.info("线程 {}：简化情况 {}，返回 {}", threadName, this, sum);
                return sum;
            }

            int mid = (begin + end) / 2;

            AddTask left = new AddTask(begin, mid);
            AddTask right = new AddTask(mid + 1, end);

            log.info("线程 {}：拆分任务 {} -> {} 和 {}", threadName, this, left, right);

            left.fork();
            right.fork();

            int leftResult = left.join();
            int rightResult = right.join();
            int total = leftResult + rightResult;

            log.info("线程 {}：合并结果 {} + {} = {}", threadName, leftResult, rightResult, total);
            return total;
        }
    }
}









