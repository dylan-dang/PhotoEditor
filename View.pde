import java.util.Vector;
import java.awt.event.KeyEvent;

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
    JToolBar[] idk = new JToolBar[10];
    for(int i = 0; i < idk.length; i++) {
      DnDTabbedPane bruh = new DnDTabbedPane();
      JTree tree = new JTree();
      bruh.addTab("test", tree);
      bruh.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      idk[i] = new JToolBar(1);
      tree.setMaximumSize(new Dimension(500, 10000000));
      idk[i].setBackground(new Color(51, 51 , 51));
      idk[i].add(bruh);
      //idk[i].add(new sampleAction("idk", null, null, null));
      add(idk[i], BorderLayout.WEST);
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
    Vector northList = new Vector();
    Vector southList = new Vector();
    Vector westList = new Vector();
    Vector eastList = new Vector();
    Vector centerList = new Vector();
    public MultiBorderLayout() {
      super();
    }

    /**
     * Constructs new layout instance with defined parameters.
     *
     * @param hgap  the horizontal gap.
     * @param vgap  the vertical gap.
     */
    public MultiBorderLayout(int hgap, int vgap) {
        super(hgap, vgap);
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
            /*
             *  Special case:  treat null the same as "Center".
             */
            if (name == null) {
                name = "Center";
            }

            /*
             *  Assign the component to one of the known regions of the layout.
             */
            if ("Center".equals(name)) {
                centerList.add(comp);
            } else if ("North".equals(name)) {
                northList.insertElementAt(comp, 0);
            } else if ("South".equals(name)) {
                southList.add(comp);
            } else if ("East".equals(name)) {
                eastList.add(comp);
            } else if ("West".equals(name)) {
                westList.add(comp);
            } else {
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

            southList.remove(comp);
            northList.remove(comp);
            centerList.remove(comp);
            westList.remove(comp);
            eastList.remove(comp);
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
    public Dimension minimumLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);

            Component c;

            if (eastList.size() > 0) {
                for (int i = 0; i < eastList.size(); i++) {
                    c = (Component) eastList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getMinimumSize();
                    dim.width += d.width + this.getHgap();
                    dim.height = Math.max(d.height, dim.height);
                }
            }
            if (westList.size() > 0) {
                for (int i = 0; i < westList.size(); i++) {
                    c = (Component) westList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getMinimumSize();
                    dim.width += d.width + this.getHgap();
                    dim.height = Math.max(d.height, dim.height);
                }
            }
            if (centerList.size() > 0) {
                for (int i = 0; i < centerList.size(); i++) {
                    c = (Component) centerList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getMinimumSize();
                    dim.width += d.width;
                    dim.height = Math.max(d.height, dim.height);
                }
            }
            if (northList.size() > 0) {
                for (int i = 0; i < northList.size(); i++) {
                    c = (Component) northList.get(i);
                    if (!c.isVisible()) {

                        continue;
                    }
                    Dimension d = c.getMinimumSize();
                    dim.width = Math.max(d.width, dim.width);
                    dim.height += d.height + this.getVgap();
                }
            }
            if (southList.size() > 0) {
                for (int i = 0; i < southList.size(); i++) {
                    c = (Component) southList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getMinimumSize();
                    dim.width = Math.max(d.width, dim.width);
                    dim.height += d.height + this.getVgap();
                }
            }

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;

            return dim;
        }

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
        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);

            Component c;

            if (eastList.size() > 0) {
                for (int i = 0; i < eastList.size(); i++) {
                    c = (Component) eastList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    dim.width += d.width + this.getHgap();
                    dim.height = Math.max(d.height, dim.height);
                }
            }

            if (westList.size() > 0) {
                for (int i = 0; i < westList.size(); i++) {
                    c = (Component) westList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    dim.width += d.width + this.getHgap();
                    dim.height = Math.max(d.height, dim.height);
                }
            }

            if (centerList.size() > 0) {
                for (int i = 0; i < centerList.size(); i++) {
                    c = (Component) centerList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    dim.width += d.width;
                    dim.height = Math.max(d.height, dim.height);
                }
            }

            if (northList.size() > 0) {
                for (int i = 0; i < northList.size(); i++) {
                    c = (Component) northList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    dim.width = Math.max(d.width, dim.width);
                    dim.height += d.height + this.getVgap();
                }
            }

            if (southList.size() > 0) {
                for (int i = 0; i < southList.size(); i++) {
                    c = (Component) southList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    dim.width = Math.max(d.width, dim.width);
                    dim.height += d.height + this.getVgap();
                }
            }

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;

            return dim;
        }
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

            if (northList.size() > 0) {
                for (int i = 0; i < northList.size(); i++) {
                    c = (Component) northList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    c.setSize(right - left, d.height);
                    c.setBounds(left, top, right - left, c.getHeight());
                    top += d.height;
                }
            }

            if (southList.size() > 0) {
                for (int i = 0; i < southList.size(); i++) {
                    c = (Component) southList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    c.setSize(right - left, d.height);
                    c.setBounds(left, bottom - d.height, right - left, c.getHeight());
                    bottom -= d.height;
                }
            }

            if (eastList.size() > 0) {
                for (int i = 0; i < eastList.size(); i++) {
                    c = (Component) eastList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    c.setSize(d.width, bottom - top);
                    c.setBounds(right - d.width, top, c.getWidth(), bottom - top);
                    right -= d.width;
                }
            }

            if (westList.size() > 0) {
                for (int i = 0; i < westList.size(); i++) {
                    c = (Component) westList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    Dimension d = c.getPreferredSize();
                    c.setSize(d.width, bottom - top);
                    c.setBounds(left, top, c.getWidth(), bottom - top);
                    left += d.width;
                }
            }

            if (centerList.size() > 0) {
                for (int i = 0; i < centerList.size(); i++) {
                    c = (Component) centerList.get(i);
                    if (!c.isVisible()) {
                        continue;
                    }
                    c.setBounds(left, top, right - left, bottom - top);
                }
            }

        }
    }
}
