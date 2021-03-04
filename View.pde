import java.util.Vector;
import java.awt.event.KeyEvent;
import java.util.HashMap;

private final Color CONTENT_BACKGROUND = new Color(0x282828);

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

  public DocumentView addDocumentView(Document doc) {
    DocumentView docView =  new DocumentView(doc);
    docView.setCanvasBackground(CONTENT_BACKGROUND);
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

import java.util.Arrays;
public class DocumentView extends JPanel {
  private Document document;
  private JPanel infoBar = new JPanel();
  private final float[] funcTable = {2,3,4,5,6,7,8,9,10,12.5,17,20,25,33.33,50,66.67,100,150,200,300,400,500,600,800,1000,1200,1400,1600,2000,2400,3200,4000,4800,5600,6400};
  private JSlider slider;
  private float scale = 1;
  private JPanel canvasWrapper = new JPanel(new GridBagLayout());
  private Canvas canvas;
  private JScrollPane scrollPane = new JScrollPane();
  private JViewport viewport = scrollPane.getViewport();

  DocumentView(Document document) {
    this.document = document;
    setLayout(new BorderLayout());
    canvasWrapper.add(canvas = new Canvas());

    infoBar.setLayout(new BoxLayout(infoBar, BoxLayout.X_AXIS));
    slider = new JSlider(JSlider.HORIZONTAL, 0, funcTable.length - 1, funcTable.length / 2);
    //slider.setPaintTicks(true);
    //slider.setMajorTickSpacing(1);
    slider.setSnapToTicks(true);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (source.getValueIsAdjusting())
        setScale(funcTable[source.getValue()] / 100);
      }
    });

    infoBar.add(Box.createGlue());
    infoBar.add(slider);
    add(infoBar, BorderLayout.SOUTH);

    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    canvasWrapper.setBackground(CONTENT_BACKGROUND);
    viewport.add(canvasWrapper);
    add(scrollPane, BorderLayout.CENTER);

    MouseAdapter scrollListener = new MouseAdapter() {
      void mouseWheelMoved(MouseWheelEvent e) {
        super.mouseWheelMoved(e);
        setScale(constrain(scale * pow(1.1, -e.getWheelRotation()), .01, 64),
                 e.getSource() instanceof JViewport ? null : e.getPoint());
      }
    };
    canvas.addMouseWheelListener(scrollListener);
    viewport.addMouseWheelListener(scrollListener);
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

  public void setCanvasBackground(Color c) {
    canvasWrapper.setBackground(c);
  }

  public JViewport getViewport() {
    return viewport;
  }

  public Canvas getCanvas() {
    return canvas;
  }

  public float getScale() {
    return scale;
  }

  public void setScale(float scale, Point pos) {
    if(pos == null) {
      setScale(scale);
      return;
    }
    float deltaScale = scale / this.scale;
    this.scale = scale;
    //if canvas is smaller than viewport, no need to translate the view position
    if(canvas.largerThan(viewport.getExtentSize())) {
      Point viewPos = viewport.getViewPosition();
      viewport.setViewPosition(new Point(
        viewPos.x + round(pos.x * deltaScale) - pos.x,
        viewPos.y + round(pos.y * deltaScale) - pos.y
      ));
    }
    int lastTick = 0;
    while (lastTick < funcTable.length && scale >= funcTable[lastTick] / 100) {
      lastTick++;
    }
    if (!slider.getValueIsAdjusting())
    slider.setValue(lastTick);

    canvas.revalidate();
    viewport.repaint();
  }

  public void setScale(float scale) {
    //default to center of viewrect
    Rectangle viewRect = viewport.getViewRect();
    setScale(scale, new Point(viewRect.x + viewRect.width / 2, viewRect.y + viewRect.height / 2));
  }
}
