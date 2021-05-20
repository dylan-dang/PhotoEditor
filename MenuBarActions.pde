private abstract class MenuBarAction extends AbstractAction {
  protected View view;
  private File file; 
  //hacky, i know. but i wouldn't be able to reference it in promptFile() and i can't make it final.

  public MenuBarAction(View view, String name, Object accelerator) {
    this.view = view;
    putValue(NAME, name);
    KeyStroke acc = null;
    if (accelerator instanceof KeyStroke) acc = (KeyStroke) accelerator;
    if (accelerator instanceof String) acc = KeyStroke.getKeyStroke((String)accelerator);
    if (acc == null) return;
    putValue(ACCELERATOR_KEY, acc);
  }

  public MenuBarAction(View view, String name) {
    this(view, name, null);
  }

  public void execute() {
    actionPerformed(new ActionEvent(view.getFrame(), ActionEvent.ACTION_FIRST, null));
  }

  protected File promptFile(final boolean isOpen) {
    final FileChooser fileChooser = new FileChooser();
    if (isOpen) fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Image Types", "*.png", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif", "*.bmp", ".dib", "*.wbmp", "*.gif"));
    fileChooser.getExtensionFilters().addAll(
       new ExtensionFilter("PNG", "*.png"),
       new ExtensionFilter("JPEG", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif"),
       new ExtensionFilter("BMP", "*.bmp", ".dib"),
       new ExtensionFilter("WBMP", "*.wbmp"),
       new ExtensionFilter("GIF", "*.gif"));
    //stop the main thread and freeze frame until a file is chosen
    final CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        view.getFrame().setEnabled(false);
        file = isOpen ? fileChooser.showOpenDialog(null) : fileChooser.showSaveDialog(null);
        latch.countDown();
        view.getFrame().setAlwaysOnTop(true);
      }
    });
    try {
      latch.await();
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    } finally {
      view.getFrame().setEnabled(true);
      view.getFrame().setAlwaysOnTop(false);
      return file;
    }
  }

  @Override
  public boolean isEnabled() {
    return view.hasSelectedDocument();
  }

  protected void updateDocView() {
    DocumentView docView = view.getSelectedDocumentView();
    docView.getCanvas().revalidate();
    docView.updateImageSizeLabel();
    view.getLayerListView().update();
  }

  protected void createSnapshot() {
    view.getSelectedDocumentView().snapShotManager.save();
  }
}

//File Menu Actions
public class NewFileAction extends MenuBarAction {
  public NewFileAction(View view) {
    super(view, "New...", "ctrl N");
    setEnabled(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
    labels.add(new JLabel("Width:", SwingConstants.RIGHT));
    labels.add(new JLabel("Height:", SwingConstants.RIGHT));

    JPanel inputs = new JPanel(new GridLayout(0, 1, 2, 2));
    JSpinner width, height;
    inputs.add(width = new JSpinner(new SpinnerNumberModel(800, 1, Short.MAX_VALUE, 1)));
    inputs.add(height = new JSpinner(new SpinnerNumberModel(600, 1, Short.MAX_VALUE, 1)));

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(labels, BorderLayout.WEST);
    panel.add(inputs, BorderLayout.CENTER);

    int confirmation = JOptionPane.showConfirmDialog(view.getFrame(), panel, "New Image", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if(confirmation == JOptionPane.CANCEL_OPTION) return;
    
    int index = view.getImageTabs().getSelectedIndex() + 1;
    view.insertDocument(new Document((int)width.getValue(), (int)height.getValue()), index);
    view.getImageTabs().setSelectedIndex(index);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}

public class OpenFileAction extends MenuBarAction {
  public OpenFileAction(View view) {
    super(view, "Open...", "ctrl O");
    setEnabled(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    File file = promptFile(true);
    if (file == null) return;
    int index = view.getImageTabs().getSelectedIndex() + 1;
    view.insertDocument(new Document(file), index);
    view.getImageTabs().setSelectedIndex(index);
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}

public class SaveAction extends MenuBarAction {
  private Document document;

  public SaveAction(View view, Document document) {
    super(view, "Save", "ctrl S");
    this.document = document;
  }

  public SaveAction(View view) {
    this(view, null);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Document doc = document == null ? view.getSelectedDocument() : document;
    if (!doc.isLinked()) {
      new SaveAsAction(view, doc).execute();
      return;
    }
    File file = doc.getLinkedFile();
    try {
      ImageIO.write(doc.flattened(), "png", file);
    } catch (IOException ioex) {
      ioex.printStackTrace();
      JOptionPane.showMessageDialog(null, "Something went wrong when trying to save your file.", "Error", JOptionPane.ERROR_MESSAGE);
    }
    doc.setSaved(true);
    view.updateTabNames();
  }
}

public class SaveAsAction extends MenuBarAction {
  private Document document;

  public SaveAsAction(View view, Document document) {
    super(view, "Save As...", "ctrl shift S");
    this.document = document;
  }

  public SaveAsAction(View view) {
    this(view, null);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Document doc = document == null ? view.getSelectedDocument() : document;
    File file = promptFile(false);
    if (file == null) return;
    doc.setLinkedFile(file);
    new SaveAction(view, doc).execute();
  }
}

public class CloseFileAction extends MenuBarAction {
  private final String[] options = {"Save", "Don't Save", "Cancel"};
  private final int SAVE = 0, DONT_SAVE = 1, CANCEL = 2;
  private int index;
  private int response = 2;

  public CloseFileAction(View view, int index) {
    super(view, "Close", "ctrl W");
    this.index = index;
  }

  public CloseFileAction(View view) {
    this(view, -1);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    int i = index == -1 ? view.getImageTabs().getSelectedIndex() : index;
    JTabbedPane imageTabs = view.getImageTabs();
    DocumentView docView = (DocumentView) imageTabs.getComponentAt(i);
    if (!docView.getDocument().isSaved()) {
      response = JOptionPane.showOptionDialog(view.getFrame(),
        String.format("Save changes to \"%s\" before closing?", docView.getDocument().getName()),
        "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[SAVE]);
      switch(response) {
        case CANCEL: return;
        case SAVE: new SaveAction(view, docView.getDocument()).execute();
      }
    } else {response = SAVE;}
    imageTabs.remove(i);
  }

  public boolean getSuccess() {
    return response != CANCEL;
  }
}

public class CloseAllAction extends MenuBarAction {
  public CloseAllAction(View view) {
    super(view, "Close All", "ctrl alt W");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final JTabbedPane imageTabs = view.getImageTabs();
    final int tabCount = imageTabs.getTabCount();
    for(int i = tabCount - 1; i >= 0; i--) {
      imageTabs.setSelectedIndex(i);
      CloseFileAction closeFileAction = new CloseFileAction(view, i);
      closeFileAction.execute();
      if (!closeFileAction.getSuccess()) {
        break;
      }
    }
  }
}

public class CloseOtherAction extends MenuBarAction {
  public CloseOtherAction(View view) {
    super(view, "Close Others", "ctrl alt P");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final JTabbedPane imageTabs = view.getImageTabs();
    final int tabCount = imageTabs.getTabCount();
    final int selected = imageTabs.getSelectedIndex();
    for(int i = tabCount - 1; i >= 0; i--) {
      if (i == selected) continue;
      imageTabs.setSelectedIndex(i);
      CloseFileAction closeFileAction = new CloseFileAction(view, i);
      closeFileAction.execute();
      if (!closeFileAction.getSuccess()) {
        break;
      }
    }
  }

  @Override
  public boolean isEnabled() {
    if (view.getImageTabs() == null) return false;
    return view.getImageTabs().getTabCount() > 1;
  }
}

public class PrintAction extends MenuBarAction {
  Document doc;

  public PrintAction(View view) {
    this(view, null);
  }

  public PrintAction(View view, Document doc) {
    super(view, "Print...", "ctrl P");
    this.doc = doc;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (doc == null) doc = view.getSelectedDocument();
    if (doc == null) return;
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setPrintable(new PrintableImage(printJob, doc.flattened()));
    if (printJob.printDialog()) {
      try {
        printJob.print();
      } catch (PrinterException prt) {
        prt.printStackTrace();
      }
    }
  }

  public class PrintableImage implements Printable {
    private double x, y, width;
    private int orientation;
    private BufferedImage image;

    public PrintableImage(PrinterJob printJob, BufferedImage image) {
      PageFormat pageFormat = printJob.defaultPage();
      this.x = pageFormat.getImageableX();
      this.y = pageFormat.getImageableY();
      this.width = pageFormat.getImageableWidth();
      this.orientation = pageFormat.getOrientation();
      this.image = image;
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
      if (pageIndex == 0) {
        int pWidth = 0;
        int pHeight = 0;
        if (orientation == PageFormat.PORTRAIT) {
          pWidth = (int) Math.min(width, (double) image.getWidth());
          pHeight = pWidth * image.getHeight() / image.getWidth();
        } else {
          pHeight = (int) Math.min(width, (double) image.getHeight());
          pWidth = pHeight * image.getWidth() / image.getHeight();
        }
        g.drawImage(image, (int) x, (int) y, pWidth, pHeight, null);
        return PAGE_EXISTS;
      } else {
        return NO_SUCH_PAGE;
      }
    }
  }
}

public class ExitAction extends MenuBarAction {
  public ExitAction(View view) {
    super(view, "Exit", "ctrl Q");
    setEnabled(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    new CloseAllAction(view).execute();
    if (view.getImageTabs().getTabCount() == 0) {
      forceExit();
    }
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
//Edit Menu Actions
public class UndoAction extends MenuBarAction {
  public UndoAction(View view) {
    super(view, "Undo", "ctrl Z");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.getSelectedDocumentView().snapShotManager.undo();
    updateDocView();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().snapShotManager.ableToUndo();
  }
}

public class RedoAction extends MenuBarAction {
  public RedoAction(View view) {
    super(view, "Redo", "ctrl shift Z");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.getSelectedDocumentView().getSnapShotManager().redo();
    updateDocView();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().getSnapShotManager().ableToRedo();
  }
}

public class EraseSelectionAction extends MenuBarAction {
  public EraseSelectionAction(View view) {
    super(view, "Erase Selection", "DELETE");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentView docView = view.getSelectedDocumentView();
    Rectangle selected = docView.getSelection().getBounds();
    Graphics2D g = docView.getSelectedLayer().getGraphics();

    createSnapshot();

    Composite before = g.getComposite();
    g.setComposite(AlphaComposite.Clear);
    g.fill(docView.getSelection());
    g.setComposite(before);

    view.getSelectedDocumentView().setSelection(null);
    updateDocView();
  }
  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
  }
}

public class FillSelectionAction extends MenuBarAction {
  public FillSelectionAction(View view) {
    super(view, "Fill Selection", "BACK_SPACE");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentView docView = view.getSelectedDocumentView();
    Graphics2D g = docView.getSelectedLayer().getGraphics();

    createSnapshot();

    g.setPaint(view.getToolBar().getColorSelector().getPrimary());
    g.fill(docView.getSelection());

    updateDocView();
  }
  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
  }
}

public class SelectAllAction extends MenuBarAction {
  public SelectAllAction(View view) {
    super(view, "Select All", "ctrl A");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Dimension size = view.getSelectedDocument().getDimension();
    Rectangle selection = new Rectangle(0, 0, size.width, size.height);
    view.getSelectedDocumentView().setSelection(selection);
  }
}

public class InvertSelectionAction extends MenuBarAction {
  public InvertSelectionAction(View view) {
    super(view, "Invert Selection", "ctrl I");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentView docView = view.getSelectedDocumentView();
    Dimension size = view.getSelectedDocument().getDimension();
    Area selection = new Area(new Rectangle(0, 0, size.width, size.height));

    selection.exclusiveOr(new Area(docView.getSelection()));

    docView.setSelection(selection);
  }
}

public class DeselectAction extends MenuBarAction {
  public DeselectAction(View view) {
    super(view, "Deselect", "ctrl D");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.getSelectedDocumentView().setSelection(null);
    updateDocView();
  }
  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
  }
}

//View Menu Actions
public class ZoomInAction extends MenuBarAction {
  Point2D pos;
  public ZoomInAction(View view) {
    super(view, "Zoom In", "ctrl EQUALS");
  }
  public void setPosition(Point2D pos) {
    this.pos = pos;
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentView docView = view.getSelectedDocumentView();
    final float[] ZOOM_TABLE = docView.ZOOM_TABLE;
    int previousIndex = 0;
    float scale = docView.getScale();

    for(int i = 0; i < ZOOM_TABLE.length; i++) {
      if (ZOOM_TABLE[i] <= scale * 100) previousIndex = i;
    }
    if (!(previousIndex + 1 < ZOOM_TABLE.length)) return;
    float newScale = ZOOM_TABLE[previousIndex + 1] / 100;
    if (pos == null) {
      docView.setScale(newScale);
    } else {
      pos.setLocation(pos.getX() * scale, pos.getY() * scale);
      docView.setScale(newScale, pos);
      pos = null;
    }
  }
}

public class ZoomOutAction extends MenuBarAction {
  Point2D pos;
  public ZoomOutAction(View view) {
    super(view, "Zoom Out", "ctrl MINUS");
  }
  public void setPosition(Point2D pos) {
    this.pos = pos;
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentView docView = view.getSelectedDocumentView();
    final float[] ZOOM_TABLE = docView.ZOOM_TABLE;
    int previousIndex = 0;
    float scale = docView.getScale();

    for(int i = 0; i < ZOOM_TABLE.length; i++) {
      if (ZOOM_TABLE[i] < scale * 100) previousIndex = i;
    }
    if (previousIndex < 1) return;
    float newScale = ZOOM_TABLE[previousIndex - 1] / 100;
    if (pos == null) {
      docView.setScale(newScale);
    } else {
      pos.setLocation(pos.getX() * scale, pos.getY() * scale);
      docView.setScale(newScale, pos);
      pos = null;
    }
  }
}

public class ZoomToWindowAction extends MenuBarAction {
  public ZoomToWindowAction(View view) {
    super(view, "Zoom to Window", "ctrl B");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentView docView = view.getSelectedDocumentView();
    Document doc = docView.getDocument();
    Dimension extentSize = docView.getViewport().getExtentSize();
    docView.setScale(Math.min(
      (float)extentSize.width/doc.getWidth(),
      (float)extentSize.height/doc.getHeight()
    ));
  }
}

public class ZoomToSelectionAction extends MenuBarAction {
  public ZoomToSelectionAction(View view) {
    super(view, "Zoom to Selection", "ctrl shift B");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentView docView = view.getSelectedDocumentView();
    Rectangle selected = docView.getSelection().getBounds();
    Dimension extentSize = docView.getViewport().getExtentSize();
    docView.setScale(Math.min(
      (float)extentSize.width/(float)selected.width,
      (float)extentSize.height/(float)selected.height
    ));

    Rectangle scaledSelection = docView.getScaledSelection().getBounds();
    docView.getViewport().setViewPosition(new Point(
      scaledSelection.x - (extentSize.width - scaledSelection.width) / 2,
      scaledSelection.y - (extentSize.height - scaledSelection.height) / 2));
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
  }
}

public class ActualSizeAction extends MenuBarAction {
  public ActualSizeAction(View view) {
    super(view, "Actual Size", "ctrl 0");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.getSelectedDocumentView().setScale(1.0f);
  }
}

public class TogglePixelGrid extends MenuBarAction {
  public TogglePixelGrid(View view) {
    super(view, "Turn Pixel Grid On");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    boolean pixelGridEnabled = !view.isPixelGridEnabled();
    view.setPixelGridEnabled(pixelGridEnabled);
    if(pixelGridEnabled) {
      putValue(NAME, "Turn Pixel Grid Off");
    } else {
      putValue(NAME, "Turn Pixel Grid On");
    }
  }
}

//Image Actions

public class CropToSelectionAction extends MenuBarAction {
  public CropToSelectionAction(View view) {
    super(view, "Crop to Selection", "ctrl shift X");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Document doc = view.getSelectedDocument();
    DocumentView docView = view.getSelectedDocumentView();

    createSnapshot();
    doc.crop(docView.getSelection().getBounds());
    docView.setSelection(null);

    updateDocView();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
  }
}

public class ResizeAction extends MenuBarAction {
  ResizeAction(View view) {
    super(view, "Resize...", "ctrl R");
  }

  class Item {
    String name;
    Object value;

    Item(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    String toString() {
      return name;
    }

    Object getValue() {
      return value;
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Document doc = view.getSelectedDocument();

    JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
    labels.add(new JLabel("Resampling:", SwingConstants.RIGHT));
    labels.add(new JLabel("Width:", SwingConstants.RIGHT));
    labels.add(new JLabel("Height:", SwingConstants.RIGHT));
    labels.add(new JLabel("Percentage:", SwingConstants.RIGHT));

    JPanel inputs = new JPanel(new GridLayout(0, 1, 2, 2));
    JComboBox resampling;
    JSpinner width, height, percentage;
    inputs.add(resampling = new JComboBox(new Item[] {
      new Item("Bicubic", RenderingHints.VALUE_INTERPOLATION_BICUBIC),
      new Item("Bilinear", RenderingHints.VALUE_INTERPOLATION_BILINEAR),
      new Item("Nearest Neighbor", RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)}));
    inputs.add(width = new JSpinner(new SpinnerNumberModel(doc.getWidth(), 1, Short.MAX_VALUE, 1)));
    inputs.add(height = new JSpinner(new SpinnerNumberModel(doc.getHeight(), 1, Short.MAX_VALUE, 1)));
    inputs.add(percentage = new JSpinner(new SpinnerNumberModel(100d, 1d, (double) Short.MAX_VALUE, 1d)));

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(labels, BorderLayout.WEST);
    panel.add(inputs, BorderLayout.CENTER);

    int confirmation = JOptionPane.showConfirmDialog(view.getFrame(), panel, "Resize", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if(confirmation == JOptionPane.CANCEL_OPTION) return;

    createSnapshot();
    doc.resize(
      (int)constrain(Math.round((Double)percentage.getValue()/100d * (int)width.getValue()), 1, Short.MAX_VALUE),
      (int)constrain(Math.round((Double)percentage.getValue()/100d * (int)height.getValue()), 1, Short.MAX_VALUE), 
      ((Item)resampling.getSelectedItem()).getValue());
    updateDocView();

  }
}

public class CanvasSizeAction extends MenuBarAction {
  CanvasSizeAction(View view) {
    super(view, "Canvas size...", "ctrl shift R");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Document doc = view.getSelectedDocument();

    JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
    labels.add(new JLabel("Width:", SwingConstants.RIGHT));
    labels.add(new JLabel("Height:", SwingConstants.RIGHT));
    labels.add(new JLabel("Anchor:", SwingConstants.RIGHT));

    JPanel inputs = new JPanel(new GridLayout(0, 1, 2, 2));
    JSpinner width, height;
    JComboBox anchor = new JComboBox(new String[] {
      "Top Left",
      "Top Center",
      "Top Right",
      "Center Left",
      "Center Center",
      "Center Right",
      "Bottom Left",
      "Bottom Center",
      "Bottom Right"
    });
    inputs.add(width = new JSpinner(new SpinnerNumberModel(doc.getWidth(), 1, Short.MAX_VALUE, 1)));
    inputs.add(height = new JSpinner(new SpinnerNumberModel(doc.getHeight(), 1, Short.MAX_VALUE, 1)));
    inputs.add(anchor);

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(labels, BorderLayout.WEST);
    panel.add(inputs, BorderLayout.CENTER);

    int confirmation = JOptionPane.showConfirmDialog(view.getFrame(), panel, "Resize", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if(confirmation == JOptionPane.CANCEL_OPTION) return;

    createSnapshot();
    doc.changeCanvasSize((int)width.getValue(), (int)height.getValue(), (String)anchor.getSelectedItem());
    updateDocView();
  }
}

public class ImageFlipHorizontalAction extends MenuBarAction {
  ImageFlipHorizontalAction(View view) {
    super(view, "Flip Horizontal");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

    createSnapshot();
    for(Layer layer: layers) {
      layer.flipHorizontally();
    }

    updateDocView();
  }
}

public class ImageFlipVerticalAction extends MenuBarAction {
  ImageFlipVerticalAction(View view) {
    super(view, "Flip Vertical");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

    createSnapshot();
    for(Layer layer: layers) {
      layer.flipVertically();
    }

    updateDocView();
  }
}

public class ImageRotate90degAction extends MenuBarAction {
  private boolean clockwise;

  ImageRotate90degAction(View view, boolean clockwise) {
    super(view, 
          String.format("Rotate 90° %s", clockwise ? "Clockwise": "Counter-Clockwise"), 
          String.format("ctrl %s", clockwise ? "H" : "G"));
    this.clockwise = clockwise;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Document document = view.getSelectedDocument();
    int width = document.getWidth();
    int height = document.getHeight();

    createSnapshot();

    for(Layer layer: document.getLayers()) {
      BufferedImage rotated = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = rotated.createGraphics();
      int center = clockwise ? (height - width) / 2 : (width - height) / 2;
      g.translate(center, center);
      g.rotate((clockwise ? 1 : 3) * PI / 2, height / 2, width / 2);
      g.drawRenderedImage(layer.getImage(), null);
      g.dispose();
      layer.setImage(rotated);
    }

    document.setHeight(width);
    document.setWidth(height);
    updateDocView();
  }

}

public class ImageRotate180degAction extends MenuBarAction {
  ImageRotate180degAction(View view) {
    super(view, "Rotate 180°", "ctrl J");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

    createSnapshot();
    for(Layer layer: layers) {
      layer.rotate180deg();
    }

    updateDocView();
  }
}

public class FlattenAction extends MenuBarAction {
  FlattenAction(View view) {
    super(view, "Flatten", "ctrl shift F");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Document document = view.getSelectedDocument();
    ArrayList<Layer> layers = document.getLayers();
    BufferedImage flattened = document.flattened();

    createSnapshot();

    layers.clear();
    layers.add(new Layer(flattened));

    view.getSelectedDocumentView().setSelectedLayerIndex(0);
    view.getLayerListView().update();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocument().getLayerCount() > 1;
  }
}

//Layer Actions

public class LayerFlipHorizontalAction extends MenuBarAction {
  LayerFlipHorizontalAction(View view) {
    super(view, "Flip Horizontal");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    createSnapshot();
    view.getSelectedDocumentView().getSelectedLayer().flipHorizontally();
    updateDocView();
  }
}

public class LayerFlipVerticalAction extends MenuBarAction {
  LayerFlipVerticalAction(View view) {
    super(view, "Flip Vertical");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    createSnapshot();
    view.getSelectedDocumentView().getSelectedLayer().flipVertically();
    updateDocView();
  }
}

public class LayerRotate180degAction extends MenuBarAction {
  LayerRotate180degAction(View view) {
    super(view, "Rotate 180°");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    createSnapshot();
    view.getSelectedDocumentView().getSelectedLayer().rotate180deg();
    updateDocView();
  }
}

public class SelectTopLayerAction extends MenuBarAction {
  SelectTopLayerAction(View view) {
    super(view, "Go to Top Layer", "ctrl alt PAGE_UP");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    int count = view.getSelectedDocument().getLayerCount() - 1;
    view.getSelectedDocumentView().setSelectedLayerIndex(count);
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().getSelectedLayerIndex() < view.getSelectedDocument().getLayerCount() - 1;
  }
}

public class SelectLayerAboveAction extends MenuBarAction {
  SelectLayerAboveAction(View view) {
    super(view, "Go to Layer Above", "alt PAGE_UP");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if(!isEnabled()) return;
    DocumentView docView = view.getSelectedDocumentView();

    docView.setSelectedLayerIndex(docView.getSelectedLayerIndex() + 1);
  }
  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().getSelectedLayerIndex() < view.getSelectedDocument().getLayerCount() - 1;
  }
}

public class SelectLayerBelowAction extends MenuBarAction {
  SelectLayerBelowAction(View view) {
    super(view, "Go to Layer Below", "alt PAGE_DOWN");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if(!isEnabled()) return;
    DocumentView docView = view.getSelectedDocumentView();

    docView.setSelectedLayerIndex(docView.getSelectedLayerIndex() - 1);
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().getSelectedLayerIndex() > 0;
  }
}

public class SelectBottomLayerAction extends MenuBarAction {
  SelectBottomLayerAction(View view) {
    super(view, "Go to Bottom Layer", "ctrl alt PAGE_DOWN");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.getSelectedDocumentView().setSelectedLayerIndex(0);
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().getSelectedLayerIndex() > 0;
  }
}

public class MoveLayerToTopAction extends MenuBarAction {
  MoveLayerToTopAction(View view) {
    super(view, "Move Layer to Top");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentView docView = view.getSelectedDocumentView();
    ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

    createSnapshot();
    layers.add(layers.remove(docView.getSelectedLayerIndex()));
    docView.setSelectedLayerIndex(layers.size() - 1);

    updateDocView();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().getSelectedLayerIndex() < view.getSelectedDocument().getLayerCount() - 1;
  }
}

public class MoveLayerToBottomAction extends MenuBarAction {
  MoveLayerToBottomAction(View view) {
    super(view, "Move Layer to Bottom");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    DocumentView docView = view.getSelectedDocumentView();
    ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

    createSnapshot();
    layers.add(0, layers.remove(docView.getSelectedLayerIndex()));
    docView.setSelectedLayerIndex(0);

    updateDocView();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && view.getSelectedDocumentView().getSelectedLayerIndex() > 0;
  }
}