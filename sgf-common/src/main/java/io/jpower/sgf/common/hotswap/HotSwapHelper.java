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

    static void checkDir(Path dirPath, String dirDesc, Logger log) throws IOException {
        Objects.requireNonNull(dirPath, dirDesc);
        if (Files.notExists(dirPath)) {
            Files.createDirectories(dirPath);
            if (log.isDebugEnabled()) {
                log.debug("Create {}={}", dirDesc, dirPath);
            }
        }
        if (!Files.isDirectory(dirPath)) {
            throw new IllegalStateException(dirDesc + " is not directory");
        }
    }

}
