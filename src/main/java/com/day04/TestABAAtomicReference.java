package com.day04;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

@Slf4j
class TestABAAtomicReference {
    static AtomicReference<String> ref = new AtomicReference<>("A");

    public static void main(String[] args) throws InterruptedException {
        log.info("main start...");
        String prev = ref.get();
        other();
        Thread.sleep(1000);
        log.info("change A->C {}", ref.compareAndSet(prev, "C"));
    }

    private static void other() {
        new Thread(() -> {
            log.info("change A->B {}", ref.compareAndSet(ref.get(), "B"));
        }, "t1").start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            log.info("change B->A {}", ref.compareAndSet(ref.get(), "A"));
        }, "t2").start();
    }
}

@Slf4j
class TestABAAtomicStampedReference {
    static AtomicStampedReference<String> ref = new AtomicStampedReference<>("A", 0);

    public static void main(String[] args) throws InterruptedException {
        log.info("main start...");
        String prev = ref.getReference();
        int stamp = ref.getStamp();
        log.info("版本 {}", stamp);
        other();
        Thread.sleep(1000);
        log.info("change A->C {}", ref.compareAndSet(prev, "C", stamp, stamp + 1));
    }

    private static void other() {
        new Thread(() -> {
            log.info("change A->B {}", ref.compareAndSet(ref.getReference(), "B",
                    ref.getStamp(), ref.getStamp() + 1));
            log.info("更新版本为 {}", ref.getStamp());
        }, "t1").start();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        new Thread(() -> {
            log.info("change B->A {}", ref.compareAndSet(ref.getReference(), "A",
                    ref.getStamp(), ref.getStamp() + 1));
            log.info("更新版本为 {}", ref.getStamp());
        }, "t2").start();
    }
}

@Slf4j
class TestABAAtomicMarkableReference {
    public static void main(String[] args) throws InterruptedException {
        GarbageBag bag = new GarbageBag("装满了垃圾");
        AtomicMarkableReference<GarbageBag> ref = new AtomicMarkableReference<>(bag, true);

        log.info("主线程 start...");
        GarbageBag prev = ref.getReference();
        log.info(prev.toString());

        new Thread(() -> {
            log.info("打扫卫生的线程 start...");
            bag.setDesc("空垃圾袋");
            while (!ref.compareAndSet(bag, bag, true, false)) {}
            log.info(bag.toString());
        }).start();

        Thread.sleep(1000);
        log.info("主线程想换一只新垃圾袋？");
        boolean success = ref.compareAndSet(prev, new GarbageBag("空垃圾袋"), true, false);
        log.info("换了么？{}", success);
        log.info(ref.getReference().toString());
    }
}

class GarbageBag {
    String desc;
    public GarbageBag(String desc) {
        this.desc = desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public String toString() {
        return super.toString() + " " + desc;
    }
}