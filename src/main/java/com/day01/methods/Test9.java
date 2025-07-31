package com.day01.methods;

import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "c.Test9")
public class Test9 {
    public static void main(String[] args) throws InterruptedException {
        TwoPhaseTermination tpt = new TwoPhaseTermination();
        tpt.start();

        Thread.sleep(3500);
        tpt.stop();
    }
}


@Slf4j(topic = "c.TwoPhaseTermination")
class TwoPhaseTermination {
    private Thread monitor;

    public void start(){
        monitor = new Thread(() -> {
            while(true) {
                Thread current = Thread.currentThread();
                if(current.isInterrupted()) {
                    log.info("料理后事");
                    break;
                }
                try {
                    Thread.sleep(1000);
                    log.info("将结果保存");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // 打断 sleep后会清除 打断标记，所以需要重新设置打断标记
                    current.interrupt();
                }

            }
        },"监控线程");
        monitor.start();
    }

    public void stop() {
        monitor.interrupt();
    }

}