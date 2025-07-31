package com.day01;

public class SyncAsyncDemo {

    // 模拟一个耗时任务
    public static void doTask(String taskName) {
        System.out.println(taskName + " 开始，线程：" + Thread.currentThread().getName());
        try {
            Thread.sleep(2000); // 模拟耗时2秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(taskName + " 结束，线程：" + Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        System.out.println("== 同步调用示例 ==");
        long syncStart = System.currentTimeMillis();

        doTask("同步任务1");
        doTask("同步任务2");

        long syncEnd = System.currentTimeMillis();
        System.out.println("同步总耗时：" + (syncEnd - syncStart) + "ms");

        System.out.println("\n== 异步调用示例 ==");
        long asyncStart = System.currentTimeMillis();

        Thread thread1 = new Thread(() -> doTask("异步任务1"));
        Thread thread2 = new Thread(() -> doTask("异步任务2"));

        thread1.start();
        thread2.start();

        // 主线程继续执行，不等待任务完成
        System.out.println("主线程继续执行，线程：" + Thread.currentThread().getName());

        try {
            // 如果想等异步任务结束再结束主线程，可以加 join（可选）
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long asyncEnd = System.currentTimeMillis();
        System.out.println("异步总耗时：" + (asyncEnd - asyncStart) + "ms");
    }
}
