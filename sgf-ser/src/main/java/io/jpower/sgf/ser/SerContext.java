package io.jpower.sgf.ser;

/**
 * @author zheng.sun
 */
class SerContext {

    private final CodedWriter writer;

    public SerContext(CodedWriter writer) {
        this.writer = writer;
    }

    public CodedWriter getWriter() {
        return writer;
    }

}
