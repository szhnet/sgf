package io.jpower.sgf.net.handler;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用来处理<a href=
 * "http://wiki.open.qq.com/wiki/TGW%E6%8E%A5%E5%85%A5%E8%AF%B4%E6%98%8E#2.1_.E6.94.B9.E9.80.A0.E5.AE.A2.E6.88.B7.E7.AB.AF.E7.A8.8B.E5.BA.8F.EF.BC.8C.E4.BD.BF.E5.BB.BA.E7.AB.8B.E8.BF.9E.E6.8E.A5.E5.90.8E.E7.9A.84.E7.AC.AC.E4.B8.80.E4.B8.AA.E5.8C.85.E5.B8.A6.E4.B8.8ATGW.E5.8C.85.E5.A4.B4"
 * >TGW</a>
 * <p>
 * <p>
 * TGW包头格式为形如：tgw_l7_forward\r\nHost:app12345.qzoneapp.com:80\r\n\r\n
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class TgwHandler extends FrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(TgwHandler.class);

    private static final int MAX_LENGTH = 50;

    private static final byte[] TGW_END = {'\r', '\n', '\r', '\n'};

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
            throws Exception {
        // 先判断前面的关键词
        int readableBytes = buffer.readableBytes();
        if (readableBytes < 3) {
            return null;
        }
        final int startReaderIdx = buffer.readerIndex();
        final short magic1 = buffer.getUnsignedByte(startReaderIdx);
        final short magic2 = buffer.getUnsignedByte(startReaderIdx + 1);
        final short magic3 = buffer.getUnsignedByte(startReaderIdx + 2);
        boolean isTgwRequest = (magic1 == 't' && magic2 == 'g' && magic3 == 'w');
        if (isTgwRequest) {
            // 要找到tgw的结尾
            boolean find = false;
            int i = 0;
            for (; i < readableBytes; i++) {
                if (i >= MAX_LENGTH) {
                    receiveWrongMsg(channel, buffer); // 超过最大长度，数据应该有误
                    return null;
                }
                if (readableBytes - i < TGW_END.length) {
                    break;
                }
                int j = 0;
                for (; j < TGW_END.length; j++) { // 与结束标识进行比较
                    byte b = buffer.getByte(startReaderIdx + i + j);
                    if (b != TGW_END[j]) {
                        break;
                    }
                }
                if (j == TGW_END.length) {
                    find = true;
                    break;
                }
            }
            if (find) {
                buffer.skipBytes(i + TGW_END.length); // 调过tgw的部分，保留后面的数据，下边的代码会将这部分数据传递下去。
            } else {
                return null;
            }
        }

        // 已不需要处理tgw，移除handler
        ctx.getPipeline().remove(this);
        return buffer.readBytes(buffer.readableBytes()); // 把数据传递给下一个handler处理
    }

    private void receiveWrongMsg(Channel channel, ChannelBuffer buf) {
        if (log.isErrorEnabled()) {
            log.error("Flash Policy error. channel={}, buf={}", channel,
                    ChannelBuffers.hexDump(buf));
        }
        channel.close();
    }

}
