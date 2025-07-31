package com.day04;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test3 {

    static class TwoPhaseTermination {
        // 监控线程
        private Thread monitorThread;
        // 停止标记
        private volatile boolean stop = false;
        // 判断是否执行过 start 方法
        private boolean starting = false;

        // 启动监控线程
        public void start() {
            synchronized (this) {
                if (starting) {
                    return;
                }
                starting = true;
                monitorThread = new Thread(() -> {
                    while (true) {
                        if (stop) {
                            log.info("料理后事");
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                            log.info("执行监控记录");
                        } catch (InterruptedException e) {
                            // 可选处理：log.debug("被中断了");
                        }
                    }
                }, "monitor");
                monitorThread.start();
            }
        }

        // 停止监控线程
        public void stop() {
            stop = true;
            monitorThread.interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TwoPhaseTermination tpt = new TwoPhaseTermination();
        tpt.start(); // 只会启动一次线程
        tpt.start(); // 后续调用无效
        tpt.start(); // 后续调用无效

        Thread.sleep(3500);
        log.info("停止监控");
        tpt.stop();
    }
}
