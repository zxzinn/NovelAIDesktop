package com.zxzinn.novelai.gui.filewindow;

import com.zxzinn.novelai.gui.common.ImagePreviewPanel;
import com.zxzinn.novelai.utils.Cache;
import com.zxzinn.novelai.utils.DatabaseConstructor;
import com.zxzinn.novelai.utils.I18nManager;
import com.zxzinn.novelai.utils.MetadataCleaner;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class FileManagerTab extends JPanel {
    private final FileTree fileTree;
    private final FilePreviewPanel filePreviewPanel;
    private final MetadataDisplayPanel metadataDisplayPanel;
    private final ControlPanel controlPanel;
    private final FileWatcher fileWatcher;
    private final Cache cache;

    public FileManagerTab() {
        setLayout(new BorderLayout());

        fileTree = new FileTree();
        filePreviewPanel = new FilePreviewPanel();
        metadataDisplayPanel = new MetadataDisplayPanel();
        controlPanel = new ControlPanel(this);
        fileWatcher = new FileWatcher(this::onFileChanged);
        cache = Cache.getInstance();

        initComponents();
        layoutComponents();
        loadWatchedFolders();
    }

    private void initComponents() {
        fileTree.addTreeSelectionListener(e -> {
            File selectedFile = fileTree.getSelectedFile();
            if (selectedFile != null && selectedFile.isFile()) {
                filePreviewPanel.previewFile(selectedFile);
                metadataDisplayPanel.displayMetadata(selectedFile);
            }
        });
    }

    private void layoutComponents() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(controlPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(fileTree), BorderLayout.CENTER);

        JPanel leftCombinedPanel = new JPanel(new BorderLayout());
        leftCombinedPanel.add(leftPanel, BorderLayout.WEST);
        leftCombinedPanel.add(filePreviewPanel, BorderLayout.CENTER);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCombinedPanel, metadataDisplayPanel);
        mainSplitPane.setResizeWeight(0.8);

        add(mainSplitPane, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                int totalWidth = getWidth();
                int leftPanelWidth = (int)(totalWidth * 0.2);
                leftPanel.setPreferredSize(new Dimension(leftPanelWidth, 0));
                mainSplitPane.setDividerLocation(0.8);
                revalidate();
            }
        });
    }

    private void loadWatchedFolders() {
        Set<String> foldersToWatch = new HashSet<>();

        // 添加程式運行目錄
        File currentDir = new File(System.getProperty("user.dir"));
        foldersToWatch.add(currentDir.getAbsolutePath());

        // 加載上次監聽的目錄
        String lastWatchedFolders = cache.getParameter("lastWatchedFolders", "");
        if (!lastWatchedFolders.isEmpty()) {
            foldersToWatch.addAll(Arrays.asList(lastWatchedFolders.split(",")));
        }

        // 監聽所有有效的資料夾
        for (String folderPath : foldersToWatch) {
            File folder = new File(folderPath);
            if (folder.exists() && folder.isDirectory()) {
                addPath(folderPath);
            } else {
                log.warn("Ignoring invalid or non-existent folder: {}", folderPath);
            }
        }

        log.info("Loaded watched folders: {}", String.join(", ", foldersToWatch));
    }

    public void addPath(String path) {
        File root = new File(path);
        if (root.exists() && root.isDirectory() && !fileTree.containsRoot(root)) {
            fileTree.addRoot(root);
            fileWatcher.watchDirectory(root.toPath());
            saveWatchedFolders();
            log.info("Added path to watch: {}", path);
        } else if (!root.exists() || !root.isDirectory()) {
            log.warn("Invalid path: {}", path);
            JOptionPane.showMessageDialog(this, I18nManager.getString("fileManager.invalidPath"));
        } else {
            log.info("Path already being watched: {}", path);
        }
    }

    public void removePath() {
        File selectedFile = fileTree.getSelectedFile();
        if (selectedFile != null) {
            fileTree.removeRoot(selectedFile);
            fileWatcher.stopWatching(selectedFile.toPath());
            saveWatchedFolders();
        }
    }

    public void saveWatchedFolders() {
        List<File> roots = fileTree.getRoots();
        String watchedFolders = roots.stream()
                .map(File::getAbsolutePath)
                .distinct()
                .collect(Collectors.joining(","));
        cache.setParameter("lastWatchedFolders", watchedFolders);
        cache.saveCache();
        log.info("Saved watched folders: {}", watchedFolders);
    }


    public void clearMetadata() {
        List<File> selectedFiles = fileTree.getSelectedFiles();
        if (!selectedFiles.isEmpty()) {
            List<File> unprocessedFiles = MetadataCleaner.clearMetadataForFiles(selectedFiles);
            if (unprocessedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(this, I18nManager.getString("fileManager.metadataCleared"));
            } else {
                String unprocessedFileNames = unprocessedFiles.stream()
                        .map(File::getName)
                        .collect(Collectors.joining(", "));
                JOptionPane.showMessageDialog(this,
                        I18nManager.getString("fileManager.metadataPartiallyCleared") + "\n" +
                                I18nManager.getString("fileManager.unprocessedFiles") + ": " + unprocessedFileNames);
            }
            if (selectedFiles.size() == 1) {
                metadataDisplayPanel.displayMetadata(selectedFiles.getFirst());
            }
        } else {
            JOptionPane.showMessageDialog(this, I18nManager.getString("fileManager.noFilesSelected"));
        }
    }

    public void constructDatabase() {
        List<File> selectedFiles = fileTree.getSelectedFiles();
        if (!selectedFiles.isEmpty()) {
            try {
                DatabaseConstructor.constructDatabase(selectedFiles);
                JOptionPane.showMessageDialog(this, I18nManager.getString("fileManager.databaseConstructed"));
            } catch (IOException ex) {
                log.error("Error constructing database: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, I18nManager.getString("fileManager.databaseConstructError") + ": " + ex.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, I18nManager.getString("fileManager.noFilesSelected"));
        }
    }

    public void shutdownFileWatcher() {
        fileWatcher.shutdown();
        log.info("FileWatcher shutdown");
    }

    private void onFileChanged(File changedFile, FileWatcher.FileChangeType changeType) {
        SwingUtilities.invokeLater(() -> {
            log.debug("File changed: {}, Change type: {}", changedFile.getPath(), changeType);

            // 更新文件樹
            switch (changeType) {
                case CREATED:
                    fileTree.updateTreeView(changedFile, true, false, false);
                    break;
                case DELETED:
                    fileTree.updateTreeView(changedFile, false, true, false);
                    break;
                case MODIFIED:
                    fileTree.updateTreeView(changedFile, false, false, true);
                    break;
            }

            // 如果變更的文件當前正在預覽中，刷新其內容
            if (filePreviewPanel.getCurrentFile() != null &&
                    filePreviewPanel.getCurrentFile().equals(changedFile)) {
                filePreviewPanel.refreshContent();
            }

            // 如果變更的文件當前正在顯示元數據，更新元數據顯示
            if (metadataDisplayPanel.getCurrentFile() != null &&
                    metadataDisplayPanel.getCurrentFile().equals(changedFile)) {
                metadataDisplayPanel.refreshMetadata();
            }
        });
    }
}