
public class View extends JPanel {
  public final Color CONTENT_BACKGROUND = new Color(0x282828);
  private final JFXPanel JFXPANEL = new JFXPanel();
  private ToolBar toolBar = new ToolBar(new ToolAction[] {
    new MoveAction(this),
    new SelectAction(this),
    new CropAction(this),
    new EyeDropAction(this),
    new BrushAction(this),
    new PencilAction(this),
    new EraserAction(this),
    new FillAction(this),
    new TextAction(this),
    new PanAction(this),
    new ZoomAction(this)
  });
  private JFrame frame;
  private JTabbedPane imageTabs;
  private JMenuBar menuBar = new JMenuBar();
  private ToolOptions toolOptions = new ToolOptions();
  private LayerListView layerList;

  View(final JFrame frame) {
    //set the frame for the view to hook into
    this.frame = frame;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        frame.setContentPane(initView());
        frame.revalidate();
      }
    });
  }

  private View initView() {
    //setup menubar
    addMenuActions(new JMenu("File"), new MenuBarAction[] {
      new NewFileAction(this),
      new OpenFileAction(this), null,
      new SaveAction(this),
      new SaveAsAction(this),
      null,
      new CloseFileAction(this),
      new CloseAllAction(this),
      new CloseOtherAction(this),
      null,
      new PrintAction(this),
      null,
      new ExitAction(this)});
    addMenuActions(new JMenu("Edit"), new MenuBarAction[] {});
    addMenuActions(new JMenu("View"), new MenuBarAction[] {});
    addMenuActions(new JMenu("Image"), new MenuBarAction[] {});
    addMenuActions(new JMenu("Layer"), new MenuBarAction[] {});
    addMenuActions(new JMenu("Filter"), new MenuBarAction[] {});
    frame.setJMenuBar(menuBar);

    setLayout(new MultiBorderLayout());
    add(toolBar, BorderLayout.WEST);
    add(toolOptions, BorderLayout.NORTH);

    imageTabs = new DnDTabbedPane();
    //imageTabs.addTab("bruh", new JPanel());

    layerList = new LayerListView(this);

    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    add(splitPane, BorderLayout.CENTER);
    splitPane.setLeftComponent(imageTabs);
    splitPane.setRightComponent(layerList);
    splitPane.setResizeWeight(1.0);
    splitPane.setBackground(CONTENT_BACKGROUND);

    imageTabs.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JTabbedPane source = (JTabbedPane) e.getSource();
        splitPane.setBackground(source.getTabCount() == 0 ? CONTENT_BACKGROUND : SwingUtilities.getRootPane(source).getContentPane().getBackground());
        layerList.update();
      }
    });


    final View view = this;
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        new ExitAction(view).execute();
      }
    });

    return this;
  }

  private void addMenuActions(JMenu menu, MenuBarAction[] actions) {
    menuBar.add(menu);

    for(MenuBarAction action: actions) {
      if (action == null) {
        menu.addSeparator();
      } else {
        menu.add(new JMenuItem(action) {
          @Override
          public boolean isEnabled() {
            if (getAction() == null) return super.isEnabled();
            boolean enabled = getAction().isEnabled();
            if (enabled == false) setArmed(false);
            getModel().setEnabled(enabled);
            return enabled;
          }
        });
      }
    }
  }

  public JFrame getFrame() {
    return this.frame;
  }

  public DocumentView addDocument(Document doc) {
    DocumentView docView =  new DocumentView(doc, this);
    docView.setCanvasBackground(CONTENT_BACKGROUND);
    imageTabs.addTab(doc.getName() + (doc.isSaved() ? "" : " *"), docView);
    return docView;
  }

  public JTabbedPane getImageTabs() {
    return imageTabs;
  }
  public Document getSelectedDocument() {
    DocumentView docView = getSelectedDocumentView();
    if (docView == null) return null;
    return docView.getDocument();
  }
  public DocumentView getSelectedDocumentView() {
    if (imageTabs == null) return null;
    return (DocumentView) imageTabs.getSelectedComponent();
  }

  public boolean hasSelectedDocument() {
    if (imageTabs == null) return false;
    return imageTabs.getSelectedComponent() != null;
  }

  public ToolBar getToolBar() {
    return toolBar;
  }
}
