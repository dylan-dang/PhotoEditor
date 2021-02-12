import java.util.Vector;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class View extends JPanel {
  private JFrame frame;
  public MenuBar menubar;
  View(final JFrame f) {
    //set the frame for the view to hook into
    this.frame = f;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        f.setContentPane(initView());
        f.revalidate();
      }
    });
  }

  class sampleAction extends AbstractAction {
    public sampleAction(String text, ImageIcon icon,
                      String desc, Integer mnemonic) {
      super(text, icon);
      putValue(SHORT_DESCRIPTION, desc);
      putValue(MNEMONIC_KEY, mnemonic);
    }
    public void actionPerformed(ActionEvent e) {
      println(e);
    }
  }

  private View initView() {
    frame.setJMenuBar(menubar = new MenuBar());
    setLayout(new MultiBorderLayout());

    /*DnDTabbedPane bruh = new DnDTabbedPane();
    JTree tree = new JTree();
    bruh.addTab("test", tree);
    ResizablePanel properties = new ResizablePanel();
    properties.add(bruh);
    add(properties, BorderLayout.EAST);
    */

    ResizablePanel[] idk = new ResizablePanel[10];
    for(int i = 0; i < idk.length; i++) {
      DnDTabbedPane bruh = new DnDTabbedPane();
      JTree tree = new JTree();
      bruh.addTab("test", tree);
      bruh.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      idk[i] = new ResizablePanel();
      tree.setMaximumSize(new Dimension(500, 10000000));
      idk[i].setBackground(new Color(51, 51 , 51));
      idk[i].add(bruh);
      //idk[i].add(new sampleAction("idk", null, null, null));
      add(idk[i], BorderLayout.EAST);
    }


    return this;
  }

  private class MenuBar extends JMenuBar {
    private JMenu file, edit, view, image, layer, filter;
    public JMenuItem[] fileMenu = new JMenuItem[9];
    MenuBar() {
      add(file = new JMenu("File"));
      add(edit = new JMenu("Edit"));
      add(view = new JMenu("View"));
      add(image = new JMenu("Image"));
      add(layer = new JMenu("Layer"));
      add(filter = new JMenu("Filter"));
      //file submenu
      file.add(fileMenu[0] = makeItem("New...", "ctrl N", "newfile"));
      file.add(fileMenu[1] = makeItem("Open...", "ctrl O", "openfile"));
      file.addSeparator();
      file.add(fileMenu[2] = makeItem("Save", "ctrl S", "savefile"));
      file.add(fileMenu[3] = makeItem("Save As...", "ctrl shift S", "saveasfile"));
      file.addSeparator();
      file.add(fileMenu[4] = makeItem("Close", "ctrl W", "closefile"));
      file.add(fileMenu[5] = makeItem("Close All", "ctrl alt W", "closeallfile"));
      file.add(fileMenu[6] = makeItem("Close Others", "ctrl alt P", "closeotherfile"));
      file.addSeparator();
      file.add(fileMenu[7] = makeItem("Print...", "ctrl P", "printfile"));
      file.addSeparator();
      file.add(fileMenu[8] = makeItem("Exit", "ctrl Q", "exit"));
      //edit submenu

    }
    private JMenuItem makeItem(String display, String accelerator, String command) {
      JMenuItem item = new JMenuItem(display);
      item.setAccelerator(KeyStroke.getKeyStroke(accelerator));
      item.setActionCommand(command);
      return item;
    }
  }

  public JFrame getFrame() {
    return this.frame;
  }
}

class MultiBorderLayout extends BorderLayout {

    private final String[] sideNames = {"North", "South", "West", "East", "Center"};
    HashMap<String, Vector> sides = new HashMap<String, Vector>();

    public MultiBorderLayout() {
      super();
      for(String sideName: sideNames) sides.put(sideName, new Vector());
    }

    /**
     * Constructs new layout instance with defined parameters.
     *
     * @param hgap  the horizontal gap.
     * @param vgap  the vertical gap.
     */
    public MultiBorderLayout(int hgap, int vgap) {
        super(hgap, vgap);
        for(String sideName: sideNames) sides.put(sideName, new Vector());
    }

    /**
     * Adds the specified component to the layout, using the specified
     * constraint object. For border layouts, the constraint must be one of the
     * following constants: <code>NORTH</code>, <code>SOUTH</code>, <code>EAST</code>
     * , <code>WEST</code>, or <code>CENTER</code>. <p>
     *
     * Most applications do not call this method directly. This method is called
     * when a component is added to a container using the <code>Container.add</code>
     * method with the same argument types.
     *
     * @param name         The feature to be added to the LayoutComponent
     *      attribute.
     * @param comp         the component to be added.
     */

    //the method is deprecated but it's necessary to override it because current class extends
    //BorderLayout to provide multiple components (toolbars)
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

    /**
     * Removes the specified component from this border layout. This method is
     * called when a container calls its <code>remove</code> or <code>removeAll</code>
     * methods. Most applications do not call this method directly.
     *
     * @param comp  the component to be removed.
     */
    public void removeLayoutComponent(Component comp) {
        synchronized (comp.getTreeLock()) {
          for(String sideName: sideNames) sides.get(sideName).remove(comp);
        }
    }

    /**
     * Determines the minimum size of the <code>target</code> container using
     * this layout manager. <p>
     *
     * This method is called when a container calls its <code>getMinimumSize</code>
     * method. Most applications do not call this method directly.
     *
     * @param target  the container in which to do the layout.
     * @return        the minimum dimensions needed to lay out the subcomponents
     *      of the specified container.
     */

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
    /**
     * Determines the preferred size of the <code>target</code> container using
     * this layout manager, based on the components in the container. <p>
     *
     * Most applications do not call this method directly. This method is called
     * when a container calls its <code>getPreferredSize</code> method.
     *
     * @param target  the container in which to do the layout.
     * @return        the preferred dimensions to lay out the subcomponents of
     *      the specified container.
     */
    public Dimension prefferedLayoutSize(Container target) {
      return getLayoutSize(target, true);
    }

    /**
     * Lays out the container argument using this border layout. <p>
     *
     * This method actually reshapes the components in the specified container
     * in order to satisfy the constraints of this <code>BorderLayout</code>
     * object. The <code>NORTH</code> and <code>SOUTH</code> components, if any,
     * are placed at the top and bottom of the container, respectively. The
     * <code>WEST</code> and <code>EAST</code> components are then placed on the
     * left and right, respectively. Finally, the <code>CENTER</code> object is
     * placed in any remaining space in the middle. <p>
     *
     * Most applications do not call this method directly. This method is called
     * when a container calls its <code>doLayout</code> method.
     *
     * @param target  the container in which to do the layout.
     */
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
