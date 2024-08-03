package com.zxzinn.novelai.gui.filewindow;

import com.zxzinn.novelai.utils.*;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class FileManagerTab extends JPanel implements UIComponent {
    private FileTree fileTree;
    private FilePreviewPanel filePreviewPanel;
    private MetadataDisplayPanel metadataDisplayPanel;
    private FileManagerControlPanel fileManagerControlPanel;
    private FileWatcher fileWatcher;
    private Cache cache;

    public FileManagerTab() {
        cache = Cache.getInstance();

        initializeComponents();
        layoutComponents();
        bindEvents();
        loadWatchedFolders();
    }

    @Override
    public void initializeComponents() {
        fileTree = new FileTree();
        filePreviewPanel = new FilePreviewPanel();
        metadataDisplayPanel = new MetadataDisplayPanel();
        fileManagerControlPanel = new FileManagerControlPanel(this);
        fileWatcher = new FileWatcher(this::onFileChanged);
    }

    @Override
    public void layoutComponents() {
        setLayout(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(fileManagerControlPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(fileTree), BorderLayout.CENTER);

        JPanel leftCombinedPanel = new JPanel(new BorderLayout());
        leftCombinedPanel.add(leftPanel, BorderLayout.WEST);
        leftCombinedPanel.add(filePreviewPanel, BorderLayout.CENTER);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftCombinedPanel, metadataDisplayPanel);
        mainSplitPane.setResizeWeight(0.8);

        add(mainSplitPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                int totalWidth = getWidth();
                int leftPanelWidth = (int)(totalWidth * 0.2);
                leftPanel.setPreferredSize(new Dimension(leftPanelWidth, 0));
                mainSplitPane.setDividerLocation(0.8);
                revalidate();
            }
        });
    }

    @Override
    public void bindEvents() {
        fileTree.addTreeSelectionListener(e -> {
            File selectedFile = fileTree.getSelectedFile();
            if (selectedFile != null && selectedFile.isFile()) {
                filePreviewPanel.previewFile(selectedFile);
                metadataDisplayPanel.displayMetadata(selectedFile);
            }
        });
    }

    private void loadWatchedFolders() {
        Set<String> foldersToWatch = new HashSet<>();

        File currentDir = new File(System.getProperty("user.dir"));
        foldersToWatch.add(currentDir.getAbsolutePath());

        String lastWatchedFolders = cache.getParameter("lastWatchedFolders", "");
        if (!lastWatchedFolders.isEmpty()) {
            foldersToWatch.addAll(Arrays.asList(lastWatchedFolders.split(",")));
        }

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

            if (filePreviewPanel.getCurrentFile() != null &&
                    filePreviewPanel.getCurrentFile().equals(changedFile)) {
                filePreviewPanel.refreshContent();
            }

            if (metadataDisplayPanel.getCurrentFile() != null &&
                    metadataDisplayPanel.getCurrentFile().equals(changedFile)) {
                metadataDisplayPanel.refreshMetadata();
            }
        });
    }
}