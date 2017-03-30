package io.jpower.sgf.net;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对Netty {@link ServerBootstrap}的简单封装
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class SocketServer {

    private static final Logger log = LoggerFactory.getLogger(SocketServer.class);

    private String name;

    private int port;

    private int ioWorkerCount;

    private ChannelPipelineFactory pipelineFactory;

    private ServerBootstrap bootstrap;

    private Map<String, Object> options;

    private boolean tcpNoDelay = true;

    /**
     * @param name            server的名字，用于进行区分
     * @param port            监听端口号
     * @param pipelineFactory pipelineFactory
     */
    public SocketServer(String name, int port, ChannelPipelineFactory pipelineFactory) {
        this(name, port, pipelineFactory, -1, null);
    }

    /**
     * @param name            server的名字，用于进行区分
     * @param port            监听端口号
     * @param pipelineFactory pipelineFactory
     * @param ioWorkerCount   I/O worker threads的数量。如果该值小于等于0，将采用Netty的默认设置。
     */
    public SocketServer(String name, int port, ChannelPipelineFactory pipelineFactory,
                        int ioWorkerCount) {
        this(name, port, pipelineFactory, ioWorkerCount, null);
    }

    /**
     * @param name            server的名字，用于进行区分
     * @param port            监听端口号
     * @param pipelineFactory pipelineFactory
     * @param ioWorkerCount   I/O worker threads的数量。如果该值小于等于0，将采用Netty的默认设置。
     * @param options         设置Netty channel的配置选项，具体见{@link Bootstrap#setOptions(Map)}
     */
    public SocketServer(String name, int port, ChannelPipelineFactory pipelineFactory,
                        int ioWorkerCount, Map<String, Object> options) {
        this.name = name;
        this.port = port;
        this.pipelineFactory = pipelineFactory;
        this.ioWorkerCount = ioWorkerCount;
        this.options = options;
    }

    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    public void start() {
        if (ioWorkerCount <= 0) {
            bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        } else {
            bootstrap = new ServerBootstrap(
                    new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                            Executors.newCachedThreadPool(), ioWorkerCount));
        }

        if (options != null) {
            bootstrap.setOptions(options);
        }

        if (tcpNoDelay) {
            bootstrap.setOption("child.tcpNoDelay", true);
        } else {
            bootstrap.setOption("child.tcpNoDelay", false);
        }

        bootstrap.setPipelineFactory(pipelineFactory);

        bootstrap.bind(new InetSocketAddress(port));

        if (log.isInfoEnabled()) {
            log.info("Start SocketServer. name={}, port={}", name, port);
        }
    }

    public void stop() {
        bootstrap.releaseExternalResources();

        if (log.isInfoEnabled()) {
            log.info("Stop SocketServer. name={}, port={}", name, port);
        }
    }

    public String getName() {
        return name;
    }

}
