package io.jpower.sgf.net.msg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import io.jpower.sgf.utils.JavaUtils;

/**
 * 默认的消息配置管理器的实现
 * <p>
 * <ul>
 * <li>采用xml格式文件</li> <br>
 * 格式如下
 * <p>
 * <pre>
 * &ltmessages&gt
 *     &ltmessage type="1" name="CgLogin" /&gt
 * &lt/messages&gt
 * </pre>
 * <p>
 * <li>如果不符合实际需求，另行独自实现MessageConfigManager即可</li>
 * </ul>
 *
 * @author zheng.sun
 */
public class DefaultMessageConfigManager extends AbstractMessageConfigManager<MessageConfig.MessageMeta>
        implements MessageConfigManager<MessageConfig.MessageMeta> {

    public DefaultMessageConfigManager() {

    }

    public DefaultMessageConfigManager(String configFileName, String messagePackage) {
        super(configFileName, messagePackage);
    }

    protected List<MessageConfig.MessageMeta> loadMessageMeta() {
        Serializer serializer = new Persister();

        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(getConfigFileName());
            MessageConfig messageCfg = serializer.read(MessageConfig.class, is, false);

            List<MessageConfig.MessageMeta> messageMetas = messageCfg.getMessageMetas();
            if (messageMetas == null) {
                messageMetas = Collections.emptyList();
            }
            return messageMetas;
        } catch (Exception e) {
            throw JavaUtils.sneakyThrow(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.error("", e);
                }
            }
        }
    }

}
