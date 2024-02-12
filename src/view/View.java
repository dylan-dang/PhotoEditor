package view;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import javax.swing.*;

import controller.actions.tool.*;
import controller.actions.menubar.*;
import controller.actions.layer.*;
import controller.filters.*;
import model.Document;

public class View extends JPanel {
    public final Color CONTENT_BACKGROUND = new Color(0x282828);
    private boolean drawPixelGrid = false;
    public Set<Integer> pressedKeys = new HashSet<>();

    private final JFrame frame;
    private JMenuBar menuBar;
    private ToolOptions toolOptions;
    private final ToolAction[] toolActions = {
            // new MoveAction(this),
            new SelectAction(this), new LassoAction(this), new PolygonalLassoTool(this),
            new CropAction(this), new EyeDropAction(this), new BrushAction(this),
            new PencilAction(this), new EraserAction(this), new FillAction(this),
            // new TextAction(this),
            new PanAction(this), new ZoomAction(this)};
    private ToolBar toolBar;
    private JSplitPane splitPane;
    private LayerListView layerListView;
    private JTabbedPane imageTabs;

    public View(final JFrame frame) {
        // injects itself int to the frame, when it's safe to do so
        this.frame = frame;
        SwingUtilities.invokeLater(() -> {
            frame.setContentPane(initView());
            frame.revalidate();
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(keyEvent -> {
                    switch (keyEvent.getID()) {
                        case KeyEvent.KEY_PRESSED -> pressedKeys.add(keyEvent.getKeyCode());
                        case KeyEvent.KEY_RELEASED -> pressedKeys.remove(keyEvent.getKeyCode());
                    }
                    return false;
                });
    }

    private View initView() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        final ExitAction exitAction = new ExitAction(this);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitAction.execute();
            }
        });
        setLayout(new MultiBorderLayout());

        setupMenuBar();
        setupToolBars();
        setupSplitPane();

        return this;
    }

    private void addMenuActions(JMenu menu, MenuBarAction[] actions) {
        // makes it easier to add custom MenuBarAction rather than repeat the same line over and
        // over.
        // also creates anonymous class that overrides JMenuItem to set their enabled status without
        // the use of listeners
        // i will admit, it took me a while to find out i also had to set the underlying model's
        // enabled status to work
        menuBar.add(menu);

        for (MenuBarAction action : actions) {
            if (action == null) {
                menu.addSeparator();
                continue;
            }
            menu.add(new JMenuItem(action) {
                @Override
                public boolean isEnabled() { // makes menuActions always refer to the MenuBarAction
                                             // to get it's enabled status
                    if (getAction() == null)
                        return super.isEnabled();
                    boolean enabled = getAction().isEnabled();
                    if (!enabled)
                        setArmed(false);
                    getModel().setEnabled(enabled);
                    return enabled;
                }
            });
        }
    }

    private void setupMenuBar() {
        frame.setJMenuBar(menuBar = new JMenuBar());
        addMenuActions(new JMenu("File"), new MenuBarAction[] {new NewFileAction(this),
                new OpenFileAction(this), null, new SaveAction(this), new SaveAsAction(this), null,
                new CloseFileAction(this), new CloseAllAction(this), new CloseOtherAction(this),
                null, new PrintAction(this), null, new ExitAction(this)});
        addMenuActions(new JMenu("Edit"),
                new MenuBarAction[] {new UndoAction(this), new RedoAction(this), null,
                        new EraseSelectionAction(this), new FillSelectionAction(this),
                        new InvertSelectionAction(this), new SelectAllAction(this),
                        new DeselectAction(this)});
        addMenuActions(new JMenu("View"),
                new MenuBarAction[] {new ZoomInAction(this), new ZoomOutAction(this),
                        new ZoomToWindowAction(this), new ZoomToSelectionAction(this),
                        new ActualSizeAction(this), null, new TogglePixelGrid(this)});
        addMenuActions(new JMenu("Image"), new MenuBarAction[] {new CropToSelectionAction(this),
                new ResizeAction(this), new CanvasSizeAction(this), null,
                new ImageFlipHorizontalAction(this), new ImageFlipVerticalAction(this), null,
                new ImageRotate90degAction(this, true), new ImageRotate90degAction(this, false),
                new ImageRotate180degAction(this), null, new FlattenAction(this)});
        addMenuActions(new JMenu("Layer"),
                new MenuBarAction[] {new AddEmptyLayerAction(this), new RemoveLayerAction(this),
                        new DuplicateLayerAction(this), new MergeLayerAction(this), null,
                        new LayerFlipHorizontalAction(this), new LayerFlipVerticalAction(this),
                        new LayerRotate180degAction(this), null, new SelectTopLayerAction(this),
                        new SelectLayerAboveAction(this), new SelectLayerBelowAction(this),
                        new SelectBottomLayerAction(this), null, new MoveLayerToTopAction(this),
                        new MoveUpLayerAction(this), new MoveDownLayerAction(this),
                        new MoveLayerToBottomAction(this)});
        addMenuActions(new JMenu("Filter"),
                new MenuBarAction[] {new PopArtFilter(this), new WarholBiggieFilter(this),
                        new FantasyFilter(this), null, new BlackAndWhiteFilter(this),
                        new SepiaFilter(this), new InvertFilter(this), new PosterizeFilter(this),
                        new ChromaticAbberationFilter(this)});
    }

    private void setupToolBars() {
        // SharedOptions.setup(sketchPath());
        add(toolBar = new ToolBar(toolActions), BorderLayout.WEST);
        add(toolOptions = new ToolOptions(toolBar), BorderLayout.NORTH);
    }

    private void setupSplitPane() {
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(1.0);
        splitPane.setBackground(CONTENT_BACKGROUND);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
        setupLayerListView();
        setupImageTabs();
    }

    private void setupLayerListView() {
        layerListView = new LayerListView(this);
        layerListView.setPreferredSize(layerListView.getMinimumSize());
        splitPane.setRightComponent(layerListView);
    }

    private void setupImageTabs() {
        imageTabs = new JTabbedPane();
        imageTabs.setFocusable(false);
        imageTabs.setMinimumSize(new Dimension(0, 0));
        imageTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        imageTabs.addChangeListener(e -> {
            splitPane.setBackground(imageTabs.getTabCount() == 0 ? CONTENT_BACKGROUND
                    : frame.getContentPane().getBackground());
            layerListView.update();
        });
        splitPane.setLeftComponent(imageTabs);
    }

    public boolean hasSelectedDocument() {
        if (imageTabs == null)
            return false;
        return imageTabs.getSelectedComponent() != null;
    }

    public DocumentView getSelectedDocumentView() {
        if (imageTabs == null)
            return null;
        return (DocumentView) imageTabs.getSelectedComponent();
    }

    public Document getSelectedDocument() {
        DocumentView docView = getSelectedDocumentView();
        if (docView == null)
            return null;
        return docView.getDocument();
    }

    public void insertDocument(Document doc, int index) {
        DocumentView docView = new DocumentView(doc, this);
        docView.setCanvasBackground(CONTENT_BACKGROUND);
        imageTabs.insertTab(doc.getName() + (doc.isSaved() ? "" : "*"), null, docView, null, index);
    }

    public void updateTabNames() {
        for (int tab = 0; tab < imageTabs.getTabCount(); tab++) {
            Document doc = ((DocumentView) imageTabs.getComponentAt(tab)).getDocument();
            imageTabs.setTitleAt(tab, doc.getName() + (doc.isSaved() ? "" : "*"));
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    public ToolOptions getToolOptions() {
        return toolOptions;
    }

    public ToolBar getToolBar() {
        return toolBar;
    }

    public JTabbedPane getImageTabs() {
        return imageTabs;
    }

    public LayerListView getLayerListView() {
        return layerListView;
    }

    public boolean isPixelGridEnabled() {
        return drawPixelGrid;
    }

    public void setPixelGridEnabled(boolean value) {
        drawPixelGrid = value;
    }
}
