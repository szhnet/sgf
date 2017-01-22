package io.jpower.sgf.net.msg;

/**
 * 共享channel的session，并且是发送给多个session时所使用的消息
 * <p>
 * <ul>
 * <li>第1部分到第3部分，与NetMessage相同</li>
 * <li>第4部分，session的数量，占用2字节，无符号，范围0 - 65535</li>
 * <li>第5部分，若干个session id, 数量由第4部分指定，每个session id，占用4个字节</li>
 * <li>第6部分，与NetMessage第5部分相同</li>
 * </ul>
 *
 * @author zheng.sun
 */
public class MultiSessionMessage extends NetMessage {

    public static final int SESSION_NUM_SIZE = 2;

    public static final int SESSION_ID_SIZE = 4;

    public static final int HEADER_SIZE = FLAG_SIZE + TYPE_SIZE + BODY_LENGTH_SIZE
            + SESSION_NUM_SIZE;

    public static final int FLAG_MUTILSESSION_SHIFT = 5;

    private int[] seesionIds;

    public int[] getSeesionIds() {
        return seesionIds;
    }

    public void setSeesionIds(int[] seesionIds) {
        this.seesionIds = seesionIds;
    }

}
