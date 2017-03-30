package io.jpower.sgf.net.msg;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 内部消息
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class InternalMessage implements IMessage {

    private String name;

    @Override
    public int getType() {
        return INTERNEL_MESSAGE_TYPE;
    }

    @Override
    public String getName() {
        if (name == null) {
            name = JavaUtils.getSimpleName(getClass());
        }
        return name;
    }

    public abstract void execute();

    @Override
    public String toString() {
        return getType() + "-" + getName();
    }

}
