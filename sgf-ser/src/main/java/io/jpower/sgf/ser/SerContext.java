package io.jpower.sgf.ser;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class SerContext {

    private final CodedWriter writer;

    SerContext(CodedWriter writer) {
        this.writer = writer;
    }

    CodedWriter getWriter() {
        return writer;
    }

}
