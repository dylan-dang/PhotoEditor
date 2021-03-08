
public class View extends JPanel {
  public final Color CONTENT_BACKGROUND = new Color(0x282828);
  public ColorSelector selector = new ColorSelector();
  private final JFXPanel jfxPanel = new JFXPanel();
  private final ToolAction[] toolActions = {
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
  };

  private JFrame frame;
  private JTabbedPane imageTabs;
  private JMenuBar menuBar = new JMenuBar();
  private ToolAction selectedTool;

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
    setLayout(new BorderLayout());
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

    add(new ToolBar(), BorderLayout.WEST);

    imageTabs = new DnDTabbedPane();
    //imageTabs.addTab("bruh", new JPanel());

    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    add(splitPane, BorderLayout.CENTER);
    splitPane.setLeftComponent(imageTabs);
    splitPane.setRightComponent(new LayerListView());
    splitPane.setResizeWeight(1.0);
    splitPane.setBackground(CONTENT_BACKGROUND);

    imageTabs.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JTabbedPane source = (JTabbedPane) e.getSource();
        splitPane.setBackground(source.getTabCount() == 0 ? CONTENT_BACKGROUND : SwingUtilities.getRootPane(source).getContentPane().getBackground());
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
        menu.add(action);
      }
    }
  }

  private class ToolBar extends JToolBar {
    ToolBar() {
      addRigidSpace(8);
      add(selector);
      addRigidSpace(8);

      ButtonGroup group = new ButtonGroup();
      for (ToolAction tool: toolActions) {
        JToggleButton button = new JToggleButton(tool);
        Dimension size = new Dimension(32, 24);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMinimumSize(size);
        button.setAlignmentX(CENTER_ALIGNMENT);
        group.add(button);
        add(button);
      }
      selectedTool = toolActions[0];
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
    return getSelectedDocumentView().getDocument();
  }
  public DocumentView getSelectedDocumentView() {
    return (DocumentView)imageTabs.getSelectedComponent();
  }

  public ToolAction getSelectedTool() {
    return selectedTool;
  }

  public void setSelectedTool(ToolAction selectedTool) {
    this.selectedTool = selectedTool;
  }
}
