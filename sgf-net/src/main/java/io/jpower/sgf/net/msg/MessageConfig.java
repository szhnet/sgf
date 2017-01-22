package io.jpower.sgf.net.msg;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * 默认的消息配置
 * <p>
 * <li>采用simple-xml进行加载</li> <br>
 * 格式如下
 * <p>
 * <pre>
 * &ltmessages&gt
 *     &ltmessage type="1" name="CgLogin" /&gt
 * &lt/messages&gt
 * </pre>
 *
 * @author zheng.sun
 */
@Root(name = "messages")
public class MessageConfig {

    @ElementList(required = false, inline = true)
    private List<MessageMeta> messageMetas;

    public List<MessageMeta> getMessageMetas() {
        return messageMetas;
    }

    @Root(name = "message")
    public static class MessageMeta {

        @Attribute
        private int type;

        @Attribute
        private String name;

        /**
         * 作为名字的前缀，用于拼接类名
         */
        @Attribute(required = false)
        private String prefix;

        /**
         * 是否由主业务worker处理
         */
        @Attribute(required = false)
        private boolean mainWorker;

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public boolean isMainWorker() {
            return mainWorker;
        }

        public void setMainWorker(boolean mainWorker) {
            this.mainWorker = mainWorker;
        }

    }

}
