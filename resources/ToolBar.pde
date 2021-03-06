private class ToolBar extends JToolBar {
  ToolAction[] toolActions = {
    new MoveAction(),
    new SelectAction(),
    new CropAction(),
    new EyeDropAction(),
    new BrushAction(),
    new PencilAction(),
    new EraserAction(),
    new FillAction(),
    new TextAction(),
    new PanAction(),
    new ZoomAction()
  };
  ToolBar() {
    ColorSelector selector = new ColorSelector();
    addRigidSpace(8);
    add(selector);
    addRigidSpace(8);
    ButtonGroup group = new ButtonGroup();

/*      for(ToolAction tool: new ToolAction() {

      }) {
      if (tool.endsWith(".png")) {
        JToggleButton button = new JToggleButton(new ImageIcon(sketchPath(String.format("resources/tools/%s", tool))));
        Dimension size = new Dimension(32, 24);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMinimumSize(size);
        add(button);
        button.setAlignmentX(CENTER_ALIGNMENT);
        group.add(button);
      }
      selecter
    }*/
    group.setSelected(group.getElements().nextElement().getModel(), true);
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
    setOrientation(JToolBar.VERTICAL);
  }

  private void addRigidSpace(int length) {
    add(Box.createRigidArea(new Dimension(length, length)));
  }
}
