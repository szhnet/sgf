package io.jpower.sgf.common.hotswap;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class InstrumentationAgent {

    /**
     * instrumentation
     */
    private static volatile Instrumentation instrumentation;

    public static Instrumentation instrumentation() {
        return instrumentation;
    }

    /**
     * Instrumentation permain
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    /**
     * Instrumentation agentmain
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        // The method is called by Attach Listener Thread
        // The agent class is loaded by system classloader
        instrumentation = inst;
    }

}
