package io.jpower.sgf.net.qp;

import io.jpower.sgf.net.msg.NetMessage;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jpower.sgf.net.NetConfig;
import io.jpower.sgf.net.NetHelper;
import io.jpower.sgf.net.NetSession;
import io.jpower.sgf.net.msg.MessageDispatcher;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public abstract class AbstractQpTopHandler extends SimpleChannelUpstreamHandler {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private MessageDispatcher messageDispatcher;

    private NetConfig netConfig;

    private QpMonitor qpMonitor;

    protected AbstractQpTopHandler(NetConfig netConfig, MessageDispatcher messageDispatcher,
                                   QpMonitor qpMonitor) {
        this.netConfig = netConfig;
        this.messageDispatcher = messageDispatcher;
        this.qpMonitor = qpMonitor;
    }

    public void setMessageDispatcher(MessageDispatcher messageDispatcher) {
        if (messageDispatcher == null) {
            throw new NullPointerException("messageDispatcher");
        }
        if (this.messageDispatcher != null) {
            throw new IllegalStateException("messageDispatcher can't change once set.");
        }
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        NetSession session = createNetSession();
        Channel channel = ctx.getChannel();
        channel.setAttachment(session);
        session.setChannel(channel);

        if (netConfig.isEnableRequestMode()) {
            qpMonitor.add(session);
        }
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        Channel channel = ctx.getChannel();
        NetSession session = (NetSession) channel.getAttachment();
        if (session != null && netConfig.isEnableRequestMode()) {
            qpMonitor.remove(session);
        }
    }

    protected abstract NetSession createNetSession();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        NetMessage message = (NetMessage) e.getMessage();

        NetSession session = (NetSession) ctx.getChannel().getAttachment();

        if (log.isDebugEnabled()) {
            log.debug("<<--{}: {}", NetHelper.getSessionDesc(session), message);

            Object body = message.getBody();
            log.debug("MessageBody: " + body);
        }

        message.setOwner(session);

        messageDispatcher.dispatch(message, null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        Object attachment = ctx.getChannel().getAttachment();
        if (attachment != null && attachment instanceof NetSession) {
            NetSession session = (NetSession) attachment;
            log.error(session + " throws exception", e.getCause());
        } else {
            log.error(e.getChannel() + " throws exception", e.getCause());
        }
    }

}
