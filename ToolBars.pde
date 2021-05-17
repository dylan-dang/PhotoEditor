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

public class ToolOptions extends StyledJToolBar implements ActionListener {
  JComboBox toolsCombo;
  ToolBar toolBar;
  ToolOptions(ToolBar toolBar) {
    this.toolBar = toolBar;
    setPreferredSize(new Dimension(32, 32));

    toolsCombo = new JComboBox();
    for(Component c: toolBar.getComponents()) {
      try {
        JToggleButton button = (JToggleButton) c;
        button.addActionListener(this);
        toolsCombo.addItem((ToolAction) button.getAction());
      } catch(Exception e) {}
    }
    toolsCombo.setRenderer(new ComboBoxRenderer());
    toolsCombo.setBackground(null);
    toolsCombo.setOpaque(false);
    toolsCombo.setFocusable(false);
    toolsCombo.addActionListener(this);
    add(toolsCombo);
    add(Box.createRigidArea(new Dimension(5, 5)));
    add(new JSeparator(JSeparator.VERTICAL));
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JComboBox) {
      toolBar.setSelectedTool(toolsCombo.getSelectedIndex());
    } else {
      toolsCombo.setSelectedIndex(toolBar.getSelectedIndex());
    }

  }
}


class ComboBoxRenderer extends JLabel implements ListCellRenderer {
  private JSeparator separator;

  public ComboBoxRenderer() {
    setOpaque(true);
    setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    separator = new JSeparator(JSeparator.HORIZONTAL);
  }

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    if (value == null) return separator;
    setFont(list.getFont());
    if (value instanceof ToolAction) {
      ToolAction tool = (ToolAction) value;
      setText(tool.getName());
      setIcon(tool.getIcon());
    } else {
      setText(value.toString());
    }
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    return this;
  }
}
