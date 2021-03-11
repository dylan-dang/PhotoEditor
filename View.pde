public class View extends JPanel {
  public final Color CONTENT_BACKGROUND = new Color(0x282828); //i could probably put this constant in a different static class
  private final JFXPanel JFXPANEL = new JFXPanel(); //this is needed for the FileChoosers
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
  private JTabbedPane imageTabs = new DnDTabbedPane();
  private JMenuBar menuBar = new JMenuBar();
  private ToolOptions toolOptions = new ToolOptions();
  private LayerListView layerListView = new LayerListView(this);
  private JFrame frame;

  View(final JFrame frame) {
    //injects itself int to the frame, when it's safe to do so
    this.frame = frame;
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        frame.setContentPane(initView());
        frame.revalidate();
      }
    });
  }

  private View initView() {
    //setup frame menubar
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

    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    add(splitPane, BorderLayout.CENTER);
    splitPane.setLeftComponent(imageTabs);
    splitPane.setRightComponent(layerListView);
    splitPane.setResizeWeight(1.0);
    splitPane.setBackground(CONTENT_BACKGROUND);

    imageTabs.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JTabbedPane source = (JTabbedPane) e.getSource();
        splitPane.setBackground(source.getTabCount() == 0 ? CONTENT_BACKGROUND : SwingUtilities.getRootPane(source).getContentPane().getBackground());
        layerListView.update();
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
    //makes it easier to add custom MenuBarAction rather than repeat the same line over and over.
    //also creates anonymous class that overrides JMenuItem to set their enabled status without the use of listeners
    //i will admit, it took me a while to find out i also had to set the underlying model's enable to work
    menuBar.add(menu);

    for(MenuBarAction action: actions) {
      if (action == null) {
        menu.addSeparator();
        continue;
      }
      menu.add(new JMenuItem(action) {
        @Override
        public boolean isEnabled() { //makes menuActions always refer to the MenuBarAction to get it's enabled status
          if (getAction() == null) return super.isEnabled();
          boolean enabled = getAction().isEnabled();
          if (enabled == false) setArmed(false);
          getModel().setEnabled(enabled);
          return enabled;
        }
      });
    }
  }

  public JFrame getFrame() {
    return frame;
  }

  public DocumentView insertDocument(Document doc, int index) {
    DocumentView docView =  new DocumentView(doc, this);
    docView.setCanvasBackground(CONTENT_BACKGROUND);
    imageTabs.insertTab(doc.getName(), null, docView, null, index);
    return docView;
  }

  //not really used but i guess im just future proofing
  public DocumentView addDocument(Document doc) {
    return insertDocument(doc, imageTabs.getTabCount());
  }

  public DocumentView getSelectedDocumentView() {
    if (imageTabs == null) return null;
    return (DocumentView) imageTabs.getSelectedComponent();
  }

  public Document getSelectedDocument() {
    DocumentView docView = getSelectedDocumentView();
    if (docView == null) return null;
    return docView.getDocument();
  }

  public boolean hasSelectedDocument() {
    if (imageTabs == null) return false;
    return imageTabs.getSelectedComponent() != null;
  }

  public JTabbedPane getImageTabs() {
    return imageTabs;
  }

  public ToolBar getToolBar() {
    return toolBar;
  }

  public ToolOptions getToolOptions() {
    return toolOptions;
  }

  public LayerListView getLayerListView() {
    return layerListView;
  }
}
