package io.jpower.sgf.ser;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class SerContext {

    private final CodedWriter writer;

    private final int containerSizeLimit;

    SerContext(CodedWriter writer, int containerSizeLimit) {
        this.writer = writer;
        this.containerSizeLimit = containerSizeLimit;
    }

    CodedWriter getWriter() {
        return writer;
    }

    public int getContainerSizeLimit() {
        return containerSizeLimit;
    }

}
