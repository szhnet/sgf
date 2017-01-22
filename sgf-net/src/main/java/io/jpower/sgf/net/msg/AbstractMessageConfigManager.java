package io.jpower.sgf.net.msg;

import java.util.Collection;
import java.util.List;

import io.jpower.sgf.collection.IntMap;
import io.jpower.sgf.collection.IntValueHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jpower.sgf.collection.IntHashMap;
import io.jpower.sgf.collection.IntValueMap;
import io.jpower.sgf.net.msg.MessageConfig.MessageMeta;

/**
 * @author zheng.sun
 */
public abstract class AbstractMessageConfigManager<T extends MessageMeta>
        implements MessageConfigManager<T> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private IntValueMap<Class<?>> clazzTypeMap;

    private IntMap<Class<?>> typeClazzMap;

    private IntMap<T> messageMetaMap;

    private String configFileName;

    private String messagePackage;

    protected AbstractMessageConfigManager() {

    }

    protected AbstractMessageConfigManager(String configFileName, String messagePackage) {
        this.configFileName = configFileName;
        this.messagePackage = messagePackage;
    }

    public void init() {
        List<T> messageMetas = loadMessageMeta();
        clazzTypeMap = new IntValueHashMap<Class<?>>();
        typeClazzMap = new IntHashMap<Class<?>>();
        messageMetaMap = new IntHashMap<T>();
        for (T msgMeta : messageMetas) {
            int msgType = msgMeta.getType();
            Class<?> msgBodyClazz = getMessageBodyClass(msgMeta.getName(), msgMeta.getPrefix());
            clazzTypeMap.put(msgBodyClazz, msgType);
            typeClazzMap.put(msgType, msgBodyClazz);
            messageMetaMap.put(msgType, msgMeta);
            init(msgMeta, msgBodyClazz);
        }
    }

    protected void init(MessageMeta msgMeta, Class<?> msgBodyClazz) {

    }

    protected abstract List<T> loadMessageMeta();

    private Class<?> getMessageBodyClass(String msgName, String prefix) {
        String clazzFullName = null;
        if (prefix == null) {
            clazzFullName = this.messagePackage + "." + msgName;
        } else {
            clazzFullName = this.messagePackage + "." + prefix + msgName; // 有prefix需要拼接到消息名前边
        }
        try {
            Class<?> clazz = Class.forName(clazzFullName);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public int getMessageType(Class<?> bodyClazz) {
        return clazzTypeMap.get(bodyClazz, -1);
    }

    @Override
    public <C> Class<C> getMessageBodyClass(int msgType) {
        @SuppressWarnings("unchecked")
        Class<C> c = (Class<C>) typeClazzMap.get(msgType);
        return c;
    }

    @Override
    public T getMessageMeta(int msgType) {
        T msgMeta = messageMetaMap.get(msgType);
        return msgMeta;
    }

    @Override
    public Collection<T> getMessageMetas() {
        return messageMetaMap.values();
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

    public String getMessagePackage() {
        return messagePackage;
    }

    public void setMessagePackage(String messagePackage) {
        this.messagePackage = messagePackage;
    }

}
