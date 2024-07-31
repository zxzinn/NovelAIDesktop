package com.zxzinn.novelai.gui.filewindow;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.*;

@Log4j2
public class FileTreeModel extends DefaultTreeModel {
    private final FileSystemView fileSystemView;

    public FileTreeModel() {
        super(new DefaultMutableTreeNode("File System"));
        this.fileSystemView = FileSystemView.getFileSystemView();
    }

    public void addRoot(File root) {
        DefaultMutableTreeNode newRootNode = new DefaultMutableTreeNode(new FileNode(root));
        ((DefaultMutableTreeNode) getRoot()).add(newRootNode);
        createChildren(newRootNode);
        nodeStructureChanged((DefaultMutableTreeNode) getRoot());
    }

    public void removeRoot(File root) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            FileNode fileNode = (FileNode) child.getUserObject();
            if (fileNode.getFile().equals(root)) {
                rootNode.remove(i);
                nodeStructureChanged(rootNode);
                break;
            }
        }
    }

    public List<File> getRoots() {
        List<File> roots = new ArrayList<>();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            FileNode fileNode = (FileNode) child.getUserObject();
            roots.add(fileNode.getFile());
        }
        return roots;
    }

    public boolean containsRoot(File file) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            FileNode fileNode = (FileNode) child.getUserObject();
            if (fileNode.getFile().equals(file)) {
                return true;
            }
        }
        return false;
    }

    private void createChildren(DefaultMutableTreeNode node) {
        FileNode fileNode = (FileNode) node.getUserObject();
        File file = fileNode.getFile();

        if (file.isDirectory()) {
            DefaultMutableTreeNode loadingNode = new DefaultMutableTreeNode("Loading...");
            node.add(loadingNode);
        }
    }

    public void loadChildren(final DefaultMutableTreeNode node) {
        FileNode fileNode = (FileNode) node.getUserObject();
        File file = fileNode.getFile();
        File[] children = fileSystemView.getFiles(file, true);
        Arrays.sort(children, (f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) {
                return -1;
            } else if (!f1.isDirectory() && f2.isDirectory()) {
                return 1;
            } else {
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        });

        node.removeAllChildren();
        for (File child : children) {
            if (!child.isHidden()) {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(child));
                insertNodeInto(childNode, node, node.getChildCount());
                if (child.isDirectory()) {
                    childNode.add(new DefaultMutableTreeNode("Loading..."));
                }
            }
        }
        nodeStructureChanged(node);
    }

    public void toggleCheckBox(DefaultMutableTreeNode node) {
        if (node.getUserObject() instanceof FileNode fileNode) {
            boolean newState = !fileNode.isSelected();
            fileNode.setSelected(newState);
            updateChildrenSelection(node, newState);
            updateParentSelection(node);
            nodeChanged(node);
        }
    }

    private void updateChildrenSelection(DefaultMutableTreeNode node, boolean selected) {
        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            if (child.getUserObject() instanceof FileNode childFileNode) {
                childFileNode.setSelected(selected);
                nodeChanged(child);
                if (childFileNode.getFile().isDirectory()) {
                    updateChildrenSelection(child, selected);
                }
            }
        }
    }

    private void updateParentSelection(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        if (parent != null && parent.getUserObject() instanceof FileNode parentFileNode) {
            boolean allChildrenSelected = true;
            boolean anyChildSelected = false;

            Enumeration<TreeNode> children = parent.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
                if (child.getUserObject() instanceof FileNode childFileNode) {
                    if (childFileNode.isSelected()) {
                        anyChildSelected = true;
                    } else {
                        allChildrenSelected = false;
                    }
                }
            }

            parentFileNode.setSelected(allChildrenSelected);
            nodeChanged(parent);

            updateParentSelection(parent);
        }
    }

    public List<File> getSelectedFiles() {
        List<File> selectedFiles = new ArrayList<>();
        collectSelectedFiles((DefaultMutableTreeNode) getRoot(), selectedFiles);
        return selectedFiles;
    }

    private void collectSelectedFiles(DefaultMutableTreeNode node, List<File> selectedFiles) {
        if (node.getUserObject() instanceof FileNode fileNode) {
            if (fileNode.isSelected()) {
                selectedFiles.add(fileNode.getFile());
            }
        }

        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            collectSelectedFiles(child, selectedFiles);
        }
    }

    public void updateTreeView(File changedFile, boolean isCreated, boolean isDeleted, boolean isModified) {
        DefaultMutableTreeNode node = findNodeForFile(changedFile);
        DefaultMutableTreeNode parentNode = findNodeForFile(changedFile.getParentFile());

        if (isDeleted && node != null) {
            removeNodeFromParent(node);
        } else if (isCreated && parentNode != null) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(new FileNode(changedFile));
            insertNodeInto(newNode, parentNode, findInsertionIndex(parentNode, newNode));
            if (changedFile.isDirectory()) {
                newNode.add(new DefaultMutableTreeNode("Loading..."));
            }
        } else if (isModified && node != null) {
            ((FileNode) node.getUserObject()).refreshFileInfo();
            nodeChanged(node);
        }
    }

    private DefaultMutableTreeNode findNodeForFile(File file) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
        Enumeration<TreeNode> e = rootNode.depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (node.getUserObject() instanceof FileNode fileNode) {
                if (fileNode.getFile().equals(file)) {
                    return node;
                }
            }
        }
        return null;
    }

    private int findInsertionIndex(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode newNode) {
        Object newUserObject = newNode.getUserObject();
        if (!(newUserObject instanceof FileNode newFileNode)) {
            log.warn("Unexpected user object type: {}", newUserObject.getClass().getName());
            return parentNode.getChildCount();
        }

        boolean isDirectory = newFileNode.getFile().isDirectory();

        for (int i = 0; i < parentNode.getChildCount(); i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parentNode.getChildAt(i);
            Object childUserObject = childNode.getUserObject();
            if (!(childUserObject instanceof FileNode childFileNode)) {
                log.warn("Unexpected child user object type: {}", childUserObject.getClass().getName());
                continue;
            }

            if (isDirectory && !childFileNode.getFile().isDirectory()) {
                return i;
            }

            if (isDirectory == childFileNode.getFile().isDirectory()) {
                if (newFileNode.getFile().getName().compareToIgnoreCase(childFileNode.getFile().getName()) < 0) {
                    return i;
                }
            }
        }

        return parentNode.getChildCount();
    }

    @Getter
    static class FileNode {
        private File file;
        @Setter
        private boolean selected;

        public FileNode(File file) {
            this.file = file;
            this.selected = false;
        }

        public void refreshFileInfo() {
            this.file = new File(file.getAbsolutePath());
        }

        @Override
        public String toString() {
            return file.getName();
        }
    }
}