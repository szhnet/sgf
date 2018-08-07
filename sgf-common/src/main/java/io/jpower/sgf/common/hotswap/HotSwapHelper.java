package io.jpower.sgf.common.hotswap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.slf4j.Logger;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
class HotSwapHelper {

    static void checkDir(Path swapClassDir, String dirDesc, Logger log) throws IOException {
        Objects.requireNonNull(swapClassDir, dirDesc);
        if (Files.notExists(swapClassDir)) {
            Files.createDirectories(swapClassDir);
            if (log.isDebugEnabled()) {
                log.debug("Create {}={}", dirDesc, swapClassDir);
            }
        }
        if (!Files.isDirectory(swapClassDir)) {
            throw new IllegalStateException(dirDesc + " is not directory");
        }
    }

}
