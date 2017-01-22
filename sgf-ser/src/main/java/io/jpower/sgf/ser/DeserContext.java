package io.jpower.sgf.ser;

/**
 * @author zheng.sun
 */
class DeserContext {

    private final CodedReader reader;

    private int byteSizeLimit;

    private int containerSizeLimit;

    public DeserContext(CodedReader reader, int byteSizeLimit, int containerSizeLimit) {
        this.reader = reader;
        this.byteSizeLimit = byteSizeLimit;
        this.containerSizeLimit = containerSizeLimit;
    }

    public CodedReader getReader() {
        return reader;
    }

    public int getByteSizeLimit() {
        return byteSizeLimit;
    }

    public void setByteSizeLimit(int byteSizeLimit) {
        this.byteSizeLimit = byteSizeLimit;
    }

    public int getContainerSizeLimit() {
        return containerSizeLimit;
    }

    public void setContainerSizeLimit(int containerSizeLimit) {
        this.containerSizeLimit = containerSizeLimit;
    }

}
