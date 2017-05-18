package io.jpower.sgf.common.hotswap;

import io.jpower.sgf.thread.SingleThreadWorker;
import io.jpower.sgf.utils.JavaUtils;

import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class HotSwapWorker extends SingleThreadWorker {

    private final Path swapClassDir;

    private final WatchService watchService;

    private boolean registed;

    private final HotSwap hotswap;

    public HotSwapWorker(String swapClassDirPath, String newClassFileDirPath) {
        this(Paths.get(swapClassDirPath), Paths.get(newClassFileDirPath), null);
    }

    public HotSwapWorker(String swapClassDirPath, String newClassFileDirPath, ClassLoader classLoader) {
        this(Paths.get(swapClassDirPath), Paths.get(newClassFileDirPath), classLoader);
    }

    public HotSwapWorker(Path swapClassDir, Path newClassFileDir) {
        this(swapClassDir, newClassFileDir, null);
    }

    public HotSwapWorker(Path swapClassDir, Path newClassFileDir, ClassLoader classLoader) {
        try {
            HotSwapHelper.checkDir(swapClassDir, "swapClassDir", log);
            this.swapClassDir = swapClassDir;

            this.watchService = FileSystems.getDefault().newWatchService();

            this.hotswap = new HotSwap(classLoader, newClassFileDir);
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

    @Override
    protected void execute() throws InterruptedException {
        if (!registed) {
            regist();
        }
        WatchKey key = watchService.take();
        for (WatchEvent<?> event : key.pollEvents()) {
            if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                if (log.isWarnEnabled()) {
                    log.warn("WatchEvent is overflow. dir={}", swapClassDir);
                }
            } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                Path swapFile = swapClassDir.resolve((Path) event.context());
                if (log.isInfoEnabled()) {
                    log.info("Hotswap file: {}", swapFile);
                }
                hotswap.swap(swapFile);
            }
        }
        if (!key.reset()) {
            registed = false;
        }
    }

    @Override
    protected void afterMainLoop() {
        try {
            watchService.close();
        } catch (IOException e) {
            log.error("Error while closing WatchService", e);
        }
    }

    private void regist() {
        try {
            if (Files.notExists(swapClassDir)) {
                Files.createDirectories(swapClassDir);
            }
            WatchKey key = swapClassDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            if (key.isValid()) {
                registed = true;
            }
        } catch (IOException e) {
            throw JavaUtils.sneakyThrow(e);
        }
    }

}
