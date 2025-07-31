package com.day03;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Test3 {
    static ReentrantLock lock = new ReentrantLock();  // 修正变量名首字母小写
    
    public static void main(String[] args) throws InterruptedException {  // 添加throws声明
        // 创建两个条件变量（休息室）
        Condition condition1 = lock.newCondition();
        Condition condition2 = lock.newCondition();

        lock.lock();
        try {
            // 进入休息室等待（需要处理InterruptedException）
            condition1.await();
        } finally {
            lock.unlock();  // 确保锁被释放
        }

        // 以下代码需要放在同步块中才能调用（修正示例）
        lock.lock();
        try {
            condition1.signal();    // 唤醒一个等待线程
            condition1.signalAll(); // 唤醒所有等待线程
        } finally {
            lock.unlock();
        }
    }
}