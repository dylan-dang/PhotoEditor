class MultiBorderLayout extends BorderLayout {
  private final String[] sideNames = {"North", "South", "West", "East", "Center"};
  private HashMap<String, Vector> sides = new HashMap<String, Vector>();

  public MultiBorderLayout() {
    super();
    for(String sideName: sideNames) sides.put(sideName, new Vector());
  }

  public MultiBorderLayout(int hgap, int vgap) {
    super(hgap, vgap);
    for(String sideName: sideNames) sides.put(sideName, new Vector());
  }

  public void addLayoutComponent(String name, Component comp) {
    synchronized (comp.getTreeLock()) {
      name = name == null ? "Center": name;
      try {
        sides.get(name).add(comp);
      } catch (Exception e) {
        throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
      }
    }
  }

  public void removeLayoutComponent(Component comp) {
    synchronized (comp.getTreeLock()) {
      for(String sideName: sideNames) sides.get(sideName).remove(comp);
    }
  }

  private Dimension getLayoutSize(Container target, boolean isPreferred) {
    synchronized (target.getTreeLock()) {
      Dimension dim = new Dimension(0, 0);
      Component c;

      for (String sideName: sideNames) {
        for (int i = 0; i < sides.get(sideName).size(); i++) {
          c = (Component) sides.get(sideName).get(i);
          if (!c.isVisible()) continue;
          Dimension d = isPreferred? c.getPreferredSize() : c.getMinimumSize();
          switch (sideName) {
          case "North":
          case "South":
            dim.width = Math.max(d.width, dim.width);
            dim.height += d.height + this.getVgap();
            break;
          case "East":
          case "West":
            dim.width += d.width + this.getHgap();
            dim.height = Math.max(d.height, dim.height);
            break;
          case "Center":
            dim.width += d.width;
            dim.height = Math.max(d.height, dim.height);
            break;
          }
        }
      }

      Insets insets = target.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;

      return dim;
    }
  }

  public Dimension minimumLayoutSize(Container target) {
    return getLayoutSize(target, false);
  }

  public Dimension prefferedLayoutSize(Container target) {
    return getLayoutSize(target, true);
  }

  public void layoutContainer(Container target) {
    synchronized (target.getTreeLock()) {
      Insets insets = target.getInsets();
      int top = insets.top;
      int bottom = target.getHeight() - insets.bottom;
      int left = insets.left;
      int right = target.getWidth() - insets.right;
      Component c;

      for (String sideName: sideNames) {
        for (int i = 0; i < sides.get(sideName).size(); i++) {
          c = (Component) sides.get(sideName).get(i);
          if (!c.isVisible()) continue;
          Dimension d = c.getPreferredSize();
          switch (sideName) {
          case "North":
            c.setSize(right - left, d.height);
            c.setBounds(left, top, right - left, c.getHeight());
            top += d.height;
            break;
          case "South":
            c.setSize(right - left, d.height);
            c.setBounds(left, bottom - d.height, right - left, c.getHeight());
            bottom -= d.height;
            break;
          case "East":
            c.setSize(d.width, bottom - top);
            c.setBounds(right - d.width, top, c.getWidth(), bottom - top);
            right -= d.width;
            break;
          case "West":
            c.setSize(d.width, bottom - top);
            c.setBounds(left, top, c.getWidth(), bottom - top);
            left += d.width;
            break;
          case "Center":
            c.setBounds(left, top, right - left, bottom - top);
            break;
          }
        }
      }
    }
  }
}
