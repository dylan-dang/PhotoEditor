package view;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import controller.actions.tool.ToolAction;


public class ToolOptions extends StyledJToolBar implements ActionListener {
    JComboBox<ToolAction> toolsCombo;
    ToolBar toolBar;
    ArrayList<JComponent> options;

    ToolOptions(ToolBar toolBar) {
        this.toolBar = toolBar;
        setMaximumSize(new Dimension(32, 32));

        toolsCombo = new JComboBox<ToolAction>();
        for (Component c : toolBar.getComponents()) {
            try {
                JToggleButton button = (JToggleButton) c;
                button.addActionListener(this);
                toolsCombo.addItem((ToolAction) button.getAction());
            } catch (Exception e) {
            }
        }
        toolsCombo.setRenderer(new ComboBoxRenderer<ToolAction>());
        toolsCombo.setBackground(null);
        toolsCombo.setOpaque(false);
        toolsCombo.setFocusable(false);
        toolsCombo.addActionListener(this);

        add(toolsCombo);
        add(Box.createRigidArea(new Dimension(5, 5)));
        add(new JSeparator(JSeparator.VERTICAL));
        add(Box.createRigidArea(new Dimension(5, 5)));

        actionPerformed(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof JComboBox) {
            toolBar.setSelectedTool(toolsCombo.getSelectedIndex());
        } else {
            toolsCombo.setSelectedIndex(toolBar.getSelectedIndex());
        }

        if (options != null)
            for (JComponent option : options)
                remove(option);
        options = toolBar.getSelectedTool().getOptions();
        for (JComponent option : options)
            add(option);

        revalidate();
        repaint();
    }
}
