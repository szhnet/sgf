package io.jpower.sgf.net;

import io.jpower.sgf.net.msg.NetMessage;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jpower.sgf.net.msg.MessageDispatcher;

/**
 * @author zheng.sun
 */
public abstract class AbstractTopHandler extends SimpleChannelUpstreamHandler {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    protected MessageDispatcher messageDispatcher;

    protected AbstractTopHandler() {

    }

    protected AbstractTopHandler(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
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

    protected NetSession createNetSession(Channel channel) {
        NetSession session = new NetSession();
        session.setId(channel.getId());
        session.setChannel(channel);
        channel.setAttachment(session);
        return session;
    }

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
