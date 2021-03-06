import java.util.Vector;
import java.awt.event.KeyEvent;
import java.util.HashMap;

private final Color CONTENT_BACKGROUND = new Color(0x282828);

public class View extends JPanel {
  private JFrame frame;
  private JTabbedPane imageTabs;
  private HashMap<String, JMenu> menus = new HashMap<String, JMenu>();

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

  private View initView() {
    //setup
    setLayout(new BorderLayout());
    //create menubar
    JMenuBar menuBar = new JMenuBar();
    for (String menuName: new String[] {"File", "Edit", "View", "Image", "Layer", "Filter"}) {
      JMenu menu = new JMenu(menuName);
      menus.put(menuName, menu);
      menuBar.add(menu);
    }
    frame.setJMenuBar(menuBar);

    add(new ToolBar(), BorderLayout.WEST);

    imageTabs = new DnDTabbedPane();
    //imageTabs.addTab("bruh", new JPanel());
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(imageTabs);
    splitPane.setRightComponent(new JTree());
    splitPane.setResizeWeight(1.0);
    splitPane.setBackground(CONTENT_BACKGROUND);

    imageTabs.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JTabbedPane source = (JTabbedPane) e.getSource();
        splitPane.setBackground(source.getTabCount() == 0 ? CONTENT_BACKGROUND : SwingUtilities.getRootPane(source).getContentPane().getBackground());
      }
    });

    add(splitPane, BorderLayout.CENTER);

    return this;
  }

  private class ToolBar extends JToolBar {
    ToolBar() {
      ColorSelector selector = new ColorSelector();
      addRigidSpace(8);
      add(selector);
      addRigidSpace(8);
      ButtonGroup group = new ButtonGroup();
      for(String tool: new File(sketchPath("resources/tools")).list()) {
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
      }
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

  public JFrame getFrame() {
    return this.frame;
  }

  public JMenu getMenu(String menuName) {
    return menus.get(menuName);
  }

  public DocumentView addDocument(Document doc) {
    DocumentView docView =  new DocumentView(doc, this);
    docView.setCanvasBackground(CONTENT_BACKGROUND);
    imageTabs.addTab(doc.getName() + (doc.isSaved() ? "" : " *"), docView);
    return docView;
  }
  public JTabbedPane getImageTabs() {
    //i was going to make it so only getting document view would be public but passing documentviews was a hassle, i can use index number this way
    return imageTabs;
  }
}

class ColorSelector extends JPanel {
  private Rectangle primaryArea = new Rectangle(),
                    secondaryArea = new Rectangle(),
                    trCorner = new Rectangle(),
                    blCorner = new Rectangle();
  private JColorChooser primary, secondary;

  ColorSelector() {
    primary = new JColorChooser(Color.black);
    secondary = new JColorChooser(Color.white);
    primary.setPreviewPanel(new JPanel());
    secondary.setPreviewPanel(new JPanel());

    setPreferredSize(new Dimension(32, 32));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Point mousePos = e.getPoint();
        if (primaryArea.contains(mousePos)) {
          primary.createDialog((ColorSelector)e.getSource(), "Color Picker (Primary Color)" , true, primary, null, null).setVisible(true);
        } else if (secondaryArea.contains(mousePos)) {
          secondary.createDialog((ColorSelector)e.getSource(), "Color Picker (Secondary Color)" , true, secondary, null, null).setVisible(true);
        } else if (blCorner.contains(mousePos)) {
          primary.setColor(Color.black);
          secondary.setColor(Color.white);
        } else if (trCorner.contains(mousePos)) {
          Color temp = primary.getColor();
          primary.setColor(secondary.getColor());
          secondary.setColor(temp);
        } else {
          return;
        }
        repaint();
      }
    });
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    final Color foreground = new Color(0xADADAD);
    //color squares
    Graphics2D g2 = (Graphics2D) g;
    drawColorArea(g2, secondaryArea, Color.black, Color.white, secondary.getColor());
    drawColorArea(g2, primaryArea,  Color.black, Color.white, primary.getColor());

    g2.scale((double)getPreferredSize().width / 32d, (double)getPreferredSize().height / 32d);

    //default
    drawColorArea(g2, new Rectangle(3, 24, 7, 7), foreground, Color.white);
    drawColorArea(g2, new Rectangle(0, 21, 7, 7), foreground, Color.black);

    g.setColor(foreground);
    Polygon arrows = new Polygon(new int[] {28, 23, 23, 21, 23, 23, 28, 28, 26, 28, 30, 28, 28},
                                 new int[] {3, 3, 1, 3, 5, 3, 3, 8, 8, 10, 8, 8, 3},
                                 13);
    g2.translate(0, -32 / getPreferredSize().height + .2);
    g.fillPolygon(arrows);
    g.drawPolygon(arrows);
    g2.dispose();
  }

  private void drawColorArea(Graphics2D g, Rectangle area, Color... fill) {
    area = (Rectangle) area.clone();
    for (Color c: fill) {
      g.setPaint(c);
      g.fill(area);
      area.grow(-1, -1);
    }
  }

  @Override
  void setPreferredSize(Dimension size) {
    super.setPreferredSize(size);
    setMaximumSize(size);
    setMinimumSize(size);
    double scalex = size.width / 32d;
    double scaley = size.height / 32d;
    primaryArea.setRect(0, 0, 20d * scalex, 20d * scaley);
    secondaryArea.setRect(11d * scalex, 11d * scaley, 20d * scalex, 20d * scaley);
    trCorner.setRect(20d * scalex, 0, 12d * scalex, 12d * scaley);
    blCorner.setRect(0, 20d * scaley, 12d * scalex, 12d * scaley);
  }
}
