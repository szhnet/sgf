package io.jpower.sgf.net.compress.lz4;

/**
 * @author zheng.sun
 */
class Lz4Constants {

    public static final int MIN_BLOCK_SIZE = 64;

    public static final int MAX_BLOCK_SIZE = 1 << 23; // 8M

    public static final int COMPRESS_METHOD_NONE = 0;

    public static final int COMPRESS_METHOD_LZ4 = 1;

}
