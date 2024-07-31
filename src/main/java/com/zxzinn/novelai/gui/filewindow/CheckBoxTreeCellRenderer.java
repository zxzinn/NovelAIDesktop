package com.zxzinn.novelai.gui.filewindow;

import lombok.Getter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class CheckBoxTreeCellRenderer extends JPanel implements TreeCellRenderer {
    @Getter
    private final JCheckBox checkBox;
    private final FileTree.FileTreeCellRenderer fileTreeCellRenderer;

    public CheckBoxTreeCellRenderer(FileTree.FileTreeCellRenderer fileTreeCellRenderer) {
        this.fileTreeCellRenderer = fileTreeCellRenderer;
        this.checkBox = new JCheckBox();
        setLayout(new BorderLayout());
        add(checkBox, BorderLayout.WEST);
        setOpaque(false);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        Component renderer = fileTreeCellRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        Object userObject = node.getUserObject();

        if (userObject instanceof FileTreeModel.FileNode fileNode) {
            checkBox.setVisible(true);
            checkBox.setSelected(fileNode.isSelected());
        } else {
            checkBox.setVisible(false);
        }

        removeAll();
        add(checkBox, BorderLayout.WEST);
        add(renderer, BorderLayout.CENTER);
        return this;
    }
}