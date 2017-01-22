package io.jpower.sgf.net;

/**
 * @author zheng.sun
 */
public class NetConfig {

    /**
     * 是否开启sequence模式
     */
    private boolean enableSequenceMode = false;

    /**
     * 共享channel模式
     */
    private boolean shareChannel = false;

    /**
     * 是否是请求响应模式
     */
    private boolean enableRequestMode = false;

    /**
     * 请求的超时时间，单位ms
     */
    private int requestTimeout = 10 * 1000;

    public NetConfig() {

    }

    public boolean isEnableSequenceMode() {
        return enableSequenceMode;
    }

    public void setEnableSequenceMode(boolean enableSequenceMode) {
        this.enableSequenceMode = enableSequenceMode;
    }

    public boolean isShareChannel() {
        return shareChannel;
    }

    public void setShareChannel(boolean shareChannel) {
        this.shareChannel = shareChannel;
    }

    public boolean isEnableRequestMode() {
        return enableRequestMode;
    }

    public void setEnableRequestMode(boolean enableRequestMode) {
        this.enableRequestMode = enableRequestMode;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

}
