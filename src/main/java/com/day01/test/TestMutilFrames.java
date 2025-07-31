package com.day01.test;

public class TestMutilFrames {
    public static void main(String[] args) {
        Thread t1 = new Thread(){
            @Override
            public void run() {
                // 断点模式 要选择 thread 不要选择 all
                method1(20);
            }
        };
        t1.setName("t1");
        t1.start();
        method1(10);
    }

    private static void method1(int x) {
        int y = x + 1;
        Object m = method2();
        System.out.println(m);
    }

    private static Object method2() {
        Object n = new Object();
        return n;
    }

}
