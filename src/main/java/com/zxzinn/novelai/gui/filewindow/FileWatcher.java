package com.zxzinn.novelai.gui.filewindow;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@Log4j2
public class FileWatcher {

    public enum FileChangeType {
        CREATED, DELETED, MODIFIED
    }
    private WatchService watchService;
    private final Map<WatchKey, Path> watchKeyToPath;
    private ExecutorService executorService;
    private final BiConsumer<File, FileChangeType> fileChangeCallback;

    public FileWatcher(BiConsumer<File, FileChangeType> fileChangeCallback) {
        this.fileChangeCallback = fileChangeCallback;
        watchKeyToPath = new HashMap<>();
        executorService = Executors.newSingleThreadExecutor();
        initializeWatchService();
    }

    private void initializeWatchService() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            log.error("Error initializing WatchService: " + e.getMessage());
        }
    }

    public void watchDirectory(Path path) {
        try {
            registerAll(path);
            if (executorService.isShutdown()) {
                executorService = Executors.newSingleThreadExecutor();
            }
            if (!executorService.isShutdown()) {
                executorService.submit(this::processEvents);
            }
        } catch (IOException e) {
            log.error("Error watching directory: " + e.getMessage());
        }
    }

    public void stopWatching(Path path) {
        watchKeyToPath.entrySet().removeIf(entry -> {
            if (entry.getValue().startsWith(path)) {
                entry.getKey().cancel();
                return true;
            }
            return false;
        });
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);
        watchKeyToPath.put(key, dir);
    }

    private void processEvents() {
        while (!Thread.currentThread().isInterrupted()) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                return;
            }

            Path dir = watchKeyToPath.get(key);
            if (dir == null) {
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path name = (Path) event.context();
                Path child = dir.resolve(name);

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    fileChangeCallback.accept(child.toFile(), FileChangeType.CREATED);
                    // ... 其他處理邏輯 ...
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    fileChangeCallback.accept(child.toFile(), FileChangeType.DELETED);
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    fileChangeCallback.accept(child.toFile(), FileChangeType.MODIFIED);
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                watchKeyToPath.remove(key);
                if (watchKeyToPath.isEmpty()) {
                    break;
                }
            }
        }
    }

    public void shutdown() {
        executorService.shutdownNow();
        try {
            watchService.close();
        } catch (IOException e) {
            log.error("Error closing WatchService: " + e.getMessage());
        }
    }
}