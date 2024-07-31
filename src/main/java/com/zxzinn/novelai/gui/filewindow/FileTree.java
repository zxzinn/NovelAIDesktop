package com.zxzinn.novelai.gui.filewindow;

import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.*;
import java.awt.*;
import java.io.*;
import java.util.List;

@Log4j2
public class FileTree extends JTree {
    private final FileTreeModel treeModel;
    private final CheckBoxTreeCellRenderer checkBoxRenderer;
    private final FileSystemView fileSystemView;

    public FileTree() {
        treeModel = new FileTreeModel();
        fileSystemView = FileSystemView.getFileSystemView();

        setModel(treeModel);
        FileTreeCellRenderer fileTreeCellRenderer = new FileTreeCellRenderer();
        checkBoxRenderer = new CheckBoxTreeCellRenderer(fileTreeCellRenderer);
        setCellRenderer(checkBoxRenderer);
        setRootVisible(false);
        setShowsRootHandles(true);

        addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            public void treeExpanded(javax.swing.event.TreeExpansionEvent event) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                if (node.getChildCount() == 1) {
                    DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) node.getFirstChild();
                    if (firstChild.getUserObject().equals("Loading...")) {
                        treeModel.loadChildren(node);
                    }
                }
            }
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent event) {}
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = getRowForLocation(e.getX(), e.getY());
                if(row == -1)
                    return;
                Rectangle rect = getRowBounds(row);
                if (rect != null && e.getX() < rect.x + checkBoxRenderer.getCheckBox().getWidth()) {
                    TreePath path = getPathForRow(row);
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    treeModel.toggleCheckBox(node);
                    repaint();
                }
            }
        });
    }

    public void addRoot(File root) {
        treeModel.addRoot(root);
    }

    public void removeRoot(File root) {
        treeModel.removeRoot(root);
    }

    public List<File> getRoots() {
        return treeModel.getRoots();
    }

    public boolean containsRoot(File file) {
        return treeModel.containsRoot(file);
    }

    public File getSelectedFile() {
        TreePath path = getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof FileTreeModel.FileNode) {
                return ((FileTreeModel.FileNode) node.getUserObject()).getFile();
            }
        }
        return null;
    }

    public List<File> getSelectedFiles() {
        return treeModel.getSelectedFiles();
    }

    public void updateTreeView(File changedFile, boolean isCreated, boolean isDeleted, boolean isModified) {
        SwingUtilities.invokeLater(() -> {
            try {
                treeModel.updateTreeView(changedFile, isCreated, isDeleted, isModified);
            } catch (Exception e) {
                log.error("Error updating tree view for file: " + changedFile.getPath(), e);
            }
        });
    }

    public class FileTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof FileTreeModel.FileNode fileNode) {
                File file = fileNode.getFile();

                setIcon(fileSystemView.getSystemIcon(file));
                setText(fileSystemView.getSystemDisplayName(file));
            } else {
                setText(userObject.toString());
            }

            return this;
        }
    }
}