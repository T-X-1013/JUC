package com.day04;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class Test5 {
    // 必须是 volatile
    private volatile int field;

    public static void main(String[] args) {
        // 创建字段更新器，更新 Test5 类中名为 "field" 的字段
        AtomicIntegerFieldUpdater<Test5> fieldUpdater =
                AtomicIntegerFieldUpdater.newUpdater(Test5.class, "field");

        Test5 test5 = new Test5();

        // 初始值为 0，CAS 修改为 10
        fieldUpdater.compareAndSet(test5, 0, 10);
        System.out.println(test5.field);  // 10

        // 10 -> 20
        fieldUpdater.compareAndSet(test5, 10, 20);
        System.out.println(test5.field);  // 20

        // CAS 失败：预期值为 10，但当前为 20
        fieldUpdater.compareAndSet(test5, 10, 30);
        System.out.println(test5.field);  // 20
    }
}
