import java.util.Vector;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class View extends JPanel {
  private JFrame frame;
  private DnDTabbedPane imageTabs;
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
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(imageTabs);
    splitPane.setRightComponent(new JTree());
    splitPane.setResizeWeight(1.0);

    add(splitPane, BorderLayout.CENTER);

    return this;
  }

  private class ToolBar extends JToolBar {
    ToolBar() {
      ColorSelector selector = new ColorSelector();
      selector.setPreferredSize(new Dimension(32, 32));
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

  public DocumentView addDocumentView(Document doc) {
    DocumentView docView =  new DocumentView(doc);
    imageTabs.addTab(doc.getName(), docView);
    return docView;
  }

  public DocumentView getSelectedDocumentView() {
    return (DocumentView) imageTabs.getSelectedComponent();
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
    drawColorArea(g2, secondaryArea, secondary.getColor());
    drawColorArea(g2, primaryArea, primary.getColor());

    g2.scale((double)getPreferredSize().width / 32, (double)getPreferredSize().height / 32);
    //default
    g.setColor(foreground);
    g.fillRect(3, 24, 7, 7);
    g.setColor(Color.white);
    g.fillRect(4, 25, 5, 5);
    g.setColor(foreground);
    g.fillRect(0, 21, 7, 7);
    g.setColor(Color.black);
    g.fillRect(1, 22, 5, 5);
    //switch arrows
    g.setColor(foreground);
    g.fillPolygon(new int[]{24, 21, 24}, new int[]{0, 3, 6}, 3);
    g.drawLine(24, 3, 28, 3);
    g.drawLine(28, 3, 28, 7);
    g.fillPolygon(new int[]{25, 28, 31}, new int[]{7, 10, 7}, 3);

    g2.dispose();
  }
  private void drawColorArea(Graphics2D g, Rectangle area, Color fill) {
    area = new Rectangle(area);
    for (Color c: new Color[] {Color.black, Color.white, fill}) {
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
    double scalex = size.width / 32;
    double scaley = size.height / 32;
    primaryArea.setRect(0, 0, 20 * scalex, 20 * scaley);
    secondaryArea.setRect(11 * scalex, 11 * scaley, 20 * scalex, 20 * scaley);
    trCorner.setRect(20 * scalex, 0, 12 * scalex, 12 * scaley);
    blCorner.setRect(0, 20*scaley, 12 * scalex, 12 * scaley);
  }
}

public class DocumentView extends JPanel {
  private Document document;
  private JPanel infoBar;
  private float scale = 1;
  private Canvas canvas;
  private JPanel canvasWrapper;
  //private boolean blocked = false;
  JScrollPane scrollPane = new JScrollPane();
  DocumentView(Document document) {
    this.document = document;
    setLayout(new BorderLayout());

    infoBar = new JPanel();
    JSlider slider = new JSlider(1, 2000, 100);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        setScale((float)((JSlider)e.getSource()).getValue() / 100f);
      }
    });
    infoBar.add(slider);
    add(infoBar, BorderLayout.SOUTH);

    canvasWrapper = new JPanel(new GridBagLayout());
    canvasWrapper.add(canvas = new Canvas());

    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPane.getViewport().add(canvasWrapper);
    add(scrollPane, BorderLayout.CENTER);

    canvas.addMouseWheelListener(new MouseAdapter() {
      void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        setScale(constrain(scale * pow(1.1, -e.getWheelRotation()), .01, 200), e.getPoint());
      }
    });
    scrollPane.getViewport().addMouseWheelListener(new MouseAdapter() {
      void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        setScale(constrain(scale * pow(1.1, -e.getWheelRotation()), .01, 200));
      }
    });
  }
  private class Canvas extends JPanel {
    private final BufferedImage image = document.getEditView();
    private int marginx, marginy;
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g.create();
      g2.scale(scale, scale);
      g2.drawImage(image, 0, 0, null);
      g2.dispose();
    }
    @Override
    public Dimension getPreferredSize() {
      return new Dimension(
        round(image.getWidth() * scale),
        round(image.getHeight() * scale)
      );
    }
    public boolean largerThan(Dimension container) {
      return image.getWidth() * scale > container.getWidth() ||
             image.getHeight() * scale > container.getHeight();
    }
  }

  public void setScale(float scale, Point pos) {
    JViewport vp = scrollPane.getViewport();
    float deltaScale = scale / this.scale;
    this.scale = scale;
    //if canvas is smaller than viewport, no need to translate the view position
    if(canvas.largerThan(vp.getExtentSize())) {
      Point viewPos = vp.getViewPosition();
      vp.setViewPosition(new Point(
        viewPos.x + round(pos.x * deltaScale) - pos.x,
        viewPos.y + round(pos.y * deltaScale) - pos.y
      ));
    }
    canvas.revalidate();
    scrollPane.getViewport().repaint();
  }

  public void setScale(float scale) {
    //default to center of viewrect
    Rectangle viewRect = scrollPane.getViewport().getViewRect();
    setScale(scale, new Point(viewRect.x + viewRect.width / 2, viewRect.y + viewRect.height / 2));
  }
}
