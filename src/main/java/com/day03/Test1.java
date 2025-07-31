package com.day03;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

@Slf4j(topic="c.Test1")
public class Test1 {
    static ReentrantLock lock = new ReentrantLock();
    public static void main(String[] args) {
        method1();
    }
    public static void method1() {
        lock.lock();
        try {
            log.info("execute method1");
            method2();
        } finally {
            lock.unlock();
        }
    }
    public static void method2() {
        lock.lock();
        try {
            log.info("execute method2");
            method3();
        } finally {
            lock.unlock();
        }
    }
    public static void method3() {
        lock.lock();
        try {
            log.info("execute method3");
        } finally {
            lock.unlock();
        }
    }

}
