package com.day04;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AtomicAdderBenchmark {

    public static void main(String[] args) {
        System.out.println("------ LongAdder ------");
        for (int i = 0; i < 5; i++) {
            demo(() -> new LongAdder(), adder -> adder.increment());
        }

        System.out.println("------ AtomicLong ------");
        for (int i = 0; i < 5; i++) {
            demo(() -> new AtomicLong(), adder -> adder.getAndIncrement());
        }
    }

    private static <T> void demo(Supplier<T> adderSupplier, Consumer<T> action) {
        T adder = adderSupplier.get();
        long start = System.nanoTime();
        List<Thread> ts = new ArrayList<>();
        // 40 个线程，每个线程累加 50 万次
        for (int i = 0; i < 40; i++) {
            ts.add(new Thread(() -> {
                for (int j = 0; j < 500000; j++) {
                    action.accept(adder);
                }
            }));
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
        System.out.println(adder + " cost: " + (end - start) / 1_000_000 + " ms");
    }
}