import java.util.Vector;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class View extends JPanel {
  private JFrame frame;
  private MenuBar menubar;
  private DnDTabbedPane imageTabs;

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
    frame.setJMenuBar(menubar = new MenuBar());
    setLayout(new BorderLayout());
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

  private class MenuBar extends JMenuBar {
    private JMenu fileMenu, editMenu, viewMenu, imageMenu, layerMenu, filterMenu;
    MenuBar() {
      add(fileMenu = new JMenu("File"));
      add(editMenu = new JMenu("Edit"));
      add(viewMenu = new JMenu("View"));
      add(imageMenu = new JMenu("Image"));
      add(layerMenu = new JMenu("Layer"));
      add(filterMenu = new JMenu("Filter"));
    }

    JMenu getFileMenu() {
      return fileMenu;
    }

    JMenu getEditMenu() {
      return editMenu;
    }

    JMenu getViewMenu() {
      return viewMenu;
    }

    JMenu getImageMenu() {
      return imageMenu;
    }

    JMenu getLayerMenu() {
      return layerMenu;
    }

    JMenu getFilterMenu() {
      return filterMenu;
    }
  }

  private class ToolBar extends JToolBar {
    ToolBar() {
      for(int i = 0; i < 20; i++)
      add(new JToggleButton("tool"));
      setOrientation(JToolBar.VERTICAL);
    }
  }

  public JFrame getFrame() {
    return this.frame;
  }

  public MenuBar getMenuBar() {
    return menubar;
  }

  public DocumentView addDocumentView(Document doc) {
    DocumentView docview = new DocumentView(doc);
    imageTabs.addTab(doc.getName(), docview);
    return docview;
  }
}

import java.awt.Graphics2D;
public class DocumentView extends JPanel {
  private Document document;
  private JPanel infoBar;
  private double scale;
  public ImageIcon imageIcon;
  public JLabel wrapper;
  float zoomFactor;
  DocumentView(Document document) {
    this.document = document;
    setLayout(new BorderLayout());

    infoBar = new JPanel();
    JSlider slider = new JSlider(1, 6400, 100);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        zoomFactor = ((JSlider)e.getSource()).getValue();
      }
    });
    infoBar.add(slider);
    add(infoBar, BorderLayout.SOUTH);

    final JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new GridBagLayout());
    centerPanel.add(new ImagePanel());

    imageIcon = new ImageIcon(document.getEditView());
    wrapper = new JLabel(imageIcon);
    JScrollPane scrollpane = new JScrollPane(wrapper);
    add(scrollpane, BorderLayout.CENTER);
  }
  private class ImagePanel extends JPanel {
      private final BufferedImage image = document.getEditView();
      @Override
      public void paintComponent(Graphics g) {
          super.paintComponent(g);
          Graphics2D g2 = (Graphics2D)g.create();
          g2.scale(scale, scale);
          g2.drawImage(image, 0, 0, null);
          g2.dispose();
      }
  }
  public void updateImage() {
    imageIcon.setImage(document.getEditView());
    wrapper.repaint();
  }
}
