package io.jpower.sgf.ser;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class DeserContext {

    private final CodedReader reader;

    private final boolean failOnUnknowEnumValue;

    DeserContext(CodedReader reader, boolean failOnUnknowEnumValue) {
        this.reader = reader;
        this.failOnUnknowEnumValue = failOnUnknowEnumValue;
    }

    CodedReader getReader() {
        return reader;
    }

    public boolean isFailOnUnknowEnumValue() {
        return failOnUnknowEnumValue;
    }

}
