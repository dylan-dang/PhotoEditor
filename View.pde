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
    for (String menuName: new String[] {"File", "Edit", "View", "Images", "Layer", "Filter"}) {
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
      ButtonGroup group = new ButtonGroup();
      for(String tool: new String[] {"eraser","eyedropper","paint","pencil","select","zoom","crop"}) {
        JToggleButton button = new JToggleButton(new ImageIcon(sketchPath("resources/tools/" + tool + ".png")));
        add(button);
        group.add(button);
      }
      setOrientation(JToolBar.VERTICAL);
    }
  }

  public JFrame getFrame() {
    return this.frame;
  }

  public JMenu getMenu(String menuName) {
    return menus.get(menuName);
  }

  public void addDocumentView(Document doc) {
    imageTabs.addTab(doc.getName(), new DocumentView(doc));
  }
}

public class DocumentView extends JPanel {
  private Document document;
  private JPanel infoBar;
  private float scale = 1;
  private DrawArea canvas;
  private JPanel centerPanel;
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

    centerPanel = new JPanel(new GridBagLayout());
    centerPanel.add(canvas = new DrawArea());

    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    scrollPane.getViewport().add(centerPanel);
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
  private class DrawArea extends JPanel {
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
