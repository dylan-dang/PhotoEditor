package view;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import controller.actions.tool.ToolAction;


public class ToolBar extends StyledJToolBar {
    private final ArrayList<ButtonModel> buttonModelList = new ArrayList<>();
    private final ColorSelector selector = new ColorSelector();
    private final ButtonGroup group = new ButtonGroup();
    private int selectedToolIndex = 0;
    private final ToolAction[] toolActions;

    ToolBar(ToolAction[] toolActions) {
        super();
        this.toolActions = toolActions;
        this.setFloatable(true); // restore floatable
        setOrientation(JToolBar.VERTICAL);

        addRigidSpace(8);
        add(selector);
        addRigidSpace(8);

        for (ToolAction tool : toolActions) {
            JToggleButton button = new JToggleButton(tool);
            Dimension size = new Dimension(32, 24);
            button.setPreferredSize(size);
            button.setMaximumSize(size);
            button.setMinimumSize(size);
            button.setFocusable(false);
            button.setAlignmentX(CENTER_ALIGNMENT);
            group.add(button);
            add(button);
            buttonModelList.add(button.getModel());
        }
        setSelectedTool(selectedToolIndex);
    }

    public void setSelectedTool(int index) {
        selectedToolIndex = index;
        group.setSelected(buttonModelList.get(index), true);
    }

    public ToolAction getSelectedTool() {
        return toolActions[getSelectedIndex()];
    }

    public int getSelectedIndex() {
        return buttonModelList.indexOf(group.getSelection());
    }

    public ColorSelector getColorSelector() {
        return selector;
    }
}


