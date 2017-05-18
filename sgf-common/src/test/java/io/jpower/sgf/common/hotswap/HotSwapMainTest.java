package io.jpower.sgf.common.hotswap;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class HotSwapMainTest {

    public static void main(String[] args) throws InterruptedException, IOException {
        HotSwapWorker w = new HotSwapWorker("/Users/szh/work/tmp/swap", "/Users/szh/work/tmp/cp");
        w.start();

        Foo foo = new Foo();
        while (true) {
            System.out.println(foo.getValue());
            TimeUnit.SECONDS.sleep(3);
        }
    }

    static class Foo {

        public int getValue() {
            return 1;
        }

    }

}
