package view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public abstract class StyledJToolBar extends JToolBar {
    StyledJToolBar() {
        // when parent changes and floating, set toolbar frame to undecorated
        // because minimum native frame width is too wide, and also looks better
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                JToolBar toolbar = (JToolBar) e.getComponent();
                if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) == 0)
                    return;
                if (!((BasicToolBarUI) toolbar.getUI()).isFloating())
                    return;
                Window window = SwingUtilities.windowForComponent(toolbar);
                if (window == null)
                    return;
                window.dispose();
                ((JDialog) window).setUndecorated(true);
                window.setVisible(true);
            }
        });
        // add border to stand out
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x2B2B2B)), getBorder()));
    }

    public void addRigidSpace(int length) {
        add(Box.createRigidArea(new Dimension(length, length)));
    }
}
