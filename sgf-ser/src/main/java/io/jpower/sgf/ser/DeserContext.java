package io.jpower.sgf.ser;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class DeserContext {

    private final CodedReader reader;

    private int byteSizeLimit;

    private int containerSizeLimit;

    DeserContext(CodedReader reader, int byteSizeLimit, int containerSizeLimit) {
        this.reader = reader;
        this.byteSizeLimit = byteSizeLimit;
        this.containerSizeLimit = containerSizeLimit;
    }

    CodedReader getReader() {
        return reader;
    }

    int getByteSizeLimit() {
        return byteSizeLimit;
    }

    public void setByteSizeLimit(int byteSizeLimit) {
        this.byteSizeLimit = byteSizeLimit;
    }

    int getContainerSizeLimit() {
        return containerSizeLimit;
    }

    public void setContainerSizeLimit(int containerSizeLimit) {
        this.containerSizeLimit = containerSizeLimit;
    }

}
