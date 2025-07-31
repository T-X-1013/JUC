package com.day04;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test2 {

    static class TPTVolatile {
        private Thread thread;
        private volatile boolean stop = false;

        public void start() {
            thread = new Thread(() -> {
                while (true) {
                    Thread current = Thread.currentThread();
                    if (stop) {
                        log.info("料理后事");
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                        log.info("将结果保存");
                    } catch (InterruptedException e) {
                        // InterruptedException 可能由 stop() 调用时的 thread.interrupt() 触发
                    }
                    // 执行监控操作
                }
            }, "监控线程");
            thread.start();
        }

        public void stop() {
            stop = true;
            thread.interrupt(); // 如果线程正在 sleep，可以打断它
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TPTVolatile t = new TPTVolatile();
        t.start();

        Thread.sleep(3500);
        log.info("stop");
        t.stop();
    }
}
