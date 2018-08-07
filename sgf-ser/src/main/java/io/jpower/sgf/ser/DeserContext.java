package io.jpower.sgf.ser;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class DeserContext {

    private final CodedReader reader;

    DeserContext(CodedReader reader) {
        this.reader = reader;
    }

    CodedReader getReader() {
        return reader;
    }

}
