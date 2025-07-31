package com.day04;

import java.util.ArrayList;
import java.util.List;

public interface Account {
    // 获取余额
    Integer getBalance();

    // 取款
    void withdraw(Integer amount);

    // 测试方法：启动 1000 个线程，每次扣 10 元
    static void demo(Account account) {
        List<Thread> ts = new ArrayList<>();
        long start = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            ts.add(new Thread(() -> account.withdraw(10)));
        }
        ts.forEach(Thread::start);
        ts.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        long end = System.nanoTime();
        System.out.println(account.getClass().getSimpleName() +
                " final balance: " + account.getBalance() +
                " cost: " + (end - start) / 1_000_000 + " ms");
    }
}
