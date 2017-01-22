/**
 *
 */
package io.jpower.sgf.ser.test;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.jpower.sgf.ser.Ser;
import io.jpower.sgf.ser.test.model.TestCurrency;
import io.jpower.sgf.ser.test.model.ObjectData;

/**
 * @author zeusgooogle@gmail.com
 * @sine 2016年3月3日 下午2:49:59
 */
public class PerformanceTest extends AbstractTest {

    private int max = 1000000;

    @Test
    public void singleThread() {
        ObjectData data = new ObjectData();
        data.setList(list);
        data.setMap(map);
        data.setCurrency(TestCurrency.GOLD);

        long startTime = System.currentTimeMillis();
        int count = 0;
        while (++count < max) {
            Ser ser = Ser.ins();
            // serialize
            byte[] bytes = ser.serialize(data);

            // deserialize
            ser.deserialize(bytes, ObjectData.class);
        }
        long endTime = System.currentTimeMillis();

        long totalTime = endTime - startTime;
        String message = String.format("single thread, serialize count: %d, total time(ms): %d, avg time: %d", count, totalTime, totalTime / count);
        System.out.println(message);
    }

    @Test
    public void multiThread() throws InterruptedException {
        final ObjectData data = new ObjectData();
        data.setList(list);
        data.setMap(map);
        data.setCurrency(TestCurrency.GOLD);

        final AtomicInteger count = new AtomicInteger(max);
        Executor executor = Executors.newFixedThreadPool(20);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < max; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Ser ser = Ser.ins();
                        // serialize
                        byte[] bytes = ser.serialize(data);

                        // deserialize
                        ser.deserialize(bytes, ObjectData.class);
                    } finally {
                        count.decrementAndGet();
                    }
                }
            });
        }

        while (count.get() > 0) {
            Thread.sleep(1);
        }

        long endTime = System.currentTimeMillis();

        long totalTime = endTime - startTime;
        String message = String.format("mutil thread , serialize count: %d, total time(ms): %d, avg time: %d", max, totalTime, totalTime / max);
        System.out.println(message);

    }

}
