package io.jpower.sgf.net.msg;

/**
 * 共享channel的session所使用的消息
 * <p>
 * <ul>
 * <li>因为要共享channel，所以消息中要携带sessionId的数据，以便可以找到session</li>
 * <li>第1部分到第3部分，与NetMessage相同</li>
 * <li>第4部分，session id，占用4字节，范围0 - 2147483647，负数部分暂时未使用</li>
 * <li>第5部分，与NetMessage相同</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class ShareChannelMessage extends NetMessage {

    public static final int SESSION_ID_SIZE = 4;

    public static final int HEADER_SIZE = FLAG_SIZE + TYPE_SIZE + BODY_LENGTH_SIZE
            + SESSION_ID_SIZE;

    private int sessionId = -1;

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

}
