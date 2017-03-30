package io.jpower.sgf.net;

import io.jpower.sgf.net.msg.MultiSessionMessage;
import io.jpower.sgf.net.msg.NetMessage;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class NetMessageHelper {

    /*
     * public static short createFlag(boolean requestMode) { int flag = 0; if
     * (requestMode) { flag = flag | 1 << NetMessage.FLAG_REQUEST_MODE_SHIFT; }
     * return (short) flag; }
     * 
     * public static boolean isRequestMode(short flag) { int val = flag & 1 <<
     * NetMessage.FLAG_REQUEST_MODE_SHIFT;
     * 
     * return val != 0; }
     */

    public static short createFlag(int flag, boolean compressed) {
        if (compressed) {
            flag = flag | 1 << MultiSessionMessage.FLAG_COMPRESSED_SHIFT;
        }
        return (short) flag;
    }

    public static short createRequestModeFlag(int flag, int requestMode) {
        flag = flag | requestMode << NetMessage.FLAG_REQUEST_MODE_SHIFT;
        return (short) flag;
    }

    public static short createMutilSessionFlag(int flag, boolean mutilSession) {
        if (mutilSession) {
            flag = flag | 1 << MultiSessionMessage.FLAG_MUTILSESSION_SHIFT;
        }
        return (short) flag;
    }

    public static boolean isCompressed(short flag) {
        int val = flag & 1 << MultiSessionMessage.FLAG_COMPRESSED_SHIFT;

        return val != 0;
    }

    public static int getRequestMode(short flag) {
        int mask = (1 << (16 - NetMessage.FLAG_REQUEST_MODE_SHIFT)) - 1;
        mask = mask << NetMessage.FLAG_REQUEST_MODE_SHIFT;
        int val = (flag & mask) >> NetMessage.FLAG_REQUEST_MODE_SHIFT;

        return val;
    }

    public static boolean isMutilSession(short flag) {
        int val = flag & 1 << MultiSessionMessage.FLAG_MUTILSESSION_SHIFT;

        return val != 0;
    }

}
