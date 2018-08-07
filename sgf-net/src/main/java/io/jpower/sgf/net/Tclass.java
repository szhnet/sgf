package io.jpower.sgf.net;

import io.jpower.sgf.net.compress.lz4.Lz4BodyCompressor;
import io.netty.buffer.ByteBufUtil;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class Tclass {

    public static void main(String[] args) throws IOException {
        t();
    }

    private static void t() throws IOException {
        Path path = Paths.get(System.getProperty("user.home") + "/work/tmp/setp");
        byte[] bytes = Files.readAllBytes(path);
        ChannelBuffer buf = ChannelBuffers.wrappedBuffer(bytes);
        System.out.println(buf.readableBytes());
        Lz4BodyCompressor compressor = new Lz4BodyCompressor();
        ChannelBuffer compBuf = compressor.compress(buf);
        System.out.println("compress: " + compBuf.readableBytes());
    }

}
