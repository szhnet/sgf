package io.jpower.sgf.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对Netty {@link ClientBootstrap}的简单封装
 *
 * @author zheng.sun
 */
public class SocketClient {

    private static final Logger log = LoggerFactory.getLogger(SocketClient.class);

    private String name;

    private int ioWorkerCount;

    private ChannelPipelineFactory pipelineFactory;

    private ClientBootstrap bootstrap;

    private Map<String, Object> options;

    private boolean tcpNoDelay = true;

    /**
     * @param name            client的名字，用于进行区分
     * @param pipelineFactory pipelineFactory
     */
    public SocketClient(String name, ChannelPipelineFactory pipelineFactory) {
        this(name, pipelineFactory, -1, null);
    }

    /**
     * @param name            client的名字，用于进行区分
     * @param pipelineFactory pipelineFactory
     * @param ioWorkerCount   I/O worker threads的数量。如果该值小于等于0，将采用Netty的默认设置。
     */
    public SocketClient(String name, ChannelPipelineFactory pipelineFactory, int ioWorkerCount) {
        this(name, pipelineFactory, ioWorkerCount, null);
    }

    /**
     * @param name            client的名字，用于进行区分
     * @param pipelineFactory pipelineFactory
     * @param ioWorkerCount   I/O worker threads的数量。如果该值小于等于0，将采用Netty的默认设置。
     * @param options         设置Netty channel的配置选项，具体见{@link Bootstrap#setOptions(Map)}
     */
    public SocketClient(String name, ChannelPipelineFactory pipelineFactory, int ioWorkerCount,
                        Map<String, Object> options) {
        this.name = name;
        this.pipelineFactory = pipelineFactory;
        this.ioWorkerCount = ioWorkerCount;
        this.options = options;
    }

    public void start() {
        if (ioWorkerCount <= 0) {
            bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        } else {
            bootstrap = new ClientBootstrap(
                    new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool(), ioWorkerCount));
        }

        if (options != null) {
            bootstrap.setOptions(options);
        }

        if (tcpNoDelay) {
            bootstrap.setOption("tcpNoDelay", true);
        } else {
            bootstrap.setOption("tcpNoDelay", false);
        }

        bootstrap.setPipelineFactory(pipelineFactory);

        if (log.isInfoEnabled()) {
            log.info("Start SocketClient. name=" + name);
        }
    }

    public void stop() {
        bootstrap.releaseExternalResources();

        if (log.isInfoEnabled()) {
            log.info("Stop SocketClient. name=" + name);
        }
    }

    public ChannelFuture connect(String host, int port) {
        InetSocketAddress addr = new InetSocketAddress(host, port);
        return connect(addr);
    }

    public ChannelFuture connect(SocketAddress remoteAddress) {
        ChannelFuture connectFuture = bootstrap.connect(remoteAddress);
        return connectFuture;
    }

    public String getName() {
        return name;
    }

}
