public abstract class StyledJToolBar extends JToolBar {
  StyledJToolBar() {
    //when parent changes and floating, set toolbar frame to undecorated
    //because minimum native frame width is too wide, and also looks better
    addHierarchyListener(new HierarchyListener() {
      @Override
      public void hierarchyChanged(HierarchyEvent e) {
          JToolBar toolbar = (JToolBar) e.getComponent();
          if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) == 0) return;
          if (!((BasicToolBarUI) toolbar.getUI()).isFloating()) return;
          Window window = SwingUtilities.windowForComponent(toolbar);
          if(window == null) return;
          window.dispose();
          ((JDialog) window).setUndecorated(true);
          window.setVisible(true);
      }
    });
    //add border to stand out
    setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(new Color(0x2B2B2B)),
      getBorder()
    ));
  }

  public void addRigidSpace(int length) {
    add(Box.createRigidArea(new Dimension(length, length)));
  }
}

public class ToolBar extends StyledJToolBar {
  private ArrayList<ButtonModel> buttonModelList = new ArrayList<ButtonModel>();
  private ColorSelector selector = new ColorSelector();
  private ButtonGroup group = new ButtonGroup();
  private int selectedToolIndex = 0;
  private ToolAction[] toolActions;

  ToolBar(ToolAction[] toolActions) {
    super();
    this.toolActions = toolActions;
    setOrientation(JToolBar.VERTICAL);

    addRigidSpace(8);
    add(selector);
    addRigidSpace(8);

    for (ToolAction tool: toolActions) {
      JToggleButton button = new JToggleButton(tool);
      Dimension size = new Dimension(32, 24);
      button.setPreferredSize(size);
      button.setMaximumSize(size);
      button.setMinimumSize(size);
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
    int index = buttonModelList.indexOf(group.getSelection());
    return toolActions[index];
  }
  public ColorSelector getColorSelector() {
    return selector;
  }
}

public class ToolOptions extends StyledJToolBar {
  ToolOptions() {
    setPreferredSize(new Dimension(32, 32));
  }
}
