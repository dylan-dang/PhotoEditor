public static class SharedOptions {
  public static JSpinner weightSpinner = new JSpinner(new SpinnerNumberModel(20f, 1f, 10000f, 1f));
  public static JToggleButton antialiasingButton = new JToggleButton("", true);
  public static JComboBox sampling = new JComboBox(new String[] {"Layer", "Image"});
  public static final int REPLACE = 0, ADD = 1, SUBTRACT = 2, INTERSECT = 3, INVERT = 4;
  public static JComboBox setOperations = new JComboBox(new String[] {
    "Replace",
    "Add (Union)",
    "Subtract",
    "Intersect",
    "Invert (xor)"});

  public static void setup(String sketchPath) {
    UIDefaults def = new UIDefaults();
    def.put("ToolBar:ToggleButton[Selected].backgroundPainter", DrawHelper.EMPTY_PAINTER);
    antialiasingButton.setIcon(new ImageIcon(sketchPath + "/resources/tools/options/aliasing.png"));
    antialiasingButton.setSelectedIcon(new ImageIcon(sketchPath + "/resources/tools/options/antialiasing.png"));
    antialiasingButton.setFocusable(false);
    antialiasingButton.setRolloverEnabled(false);
    antialiasingButton.putClientProperty("Nimbus.Overrides", def);
    antialiasingButton.setPreferredSize(new Dimension(24, 24));
  }
}

public abstract class ToolAction extends AbstractAction {
  protected String name;
  protected ArrayList<JComponent> options = new ArrayList<JComponent>();
  protected DragGesture dragState = new DragGesture();
  protected Point2D start, last, current;
  protected DocumentView docView;
  protected Document doc;
  protected Set buttons;
  protected Layer selectedLayer;
  protected ColorSelector selector;
  protected Rectangle imageRect;
  protected String toolTip;
  protected ToolOptions toolOptions;
  View view;

  ToolAction(String name, String toolIconName, View view) {
    this.name = name;
    this.view = view;
    this.toolOptions = view.getToolOptions();
    toolTip = name;
    putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/tools/%s", toolIconName))));
  }
  public String getName() {
    return name;
  }

  public String getToolTip() {
    return toolTip;
  }

  public ImageIcon getIcon() {
    return (ImageIcon) getValue(Action.SMALL_ICON);
  }

  public void actionPerformed(ActionEvent e) {
    if (!view.hasSelectedDocument()) return;
    DocumentView docView = view.getSelectedDocumentView();
    docView.setToolTipIcon((ImageIcon) getValue(Action.SMALL_ICON));
    docView.setToolTipText(toolTip);
  }

  public ArrayList<JComponent> getOptions() {
    return options;
  }

  public DragGesture getDragState() {
    return dragState;
  }

  public void dragStarted() {}

  public void dragging() {}

  public void initVars() {
    start = dragState.getStart();
    current = dragState.getCurrent();
    last = dragState.getLast();
    buttons = dragState.getButtons();
    docView = view.getSelectedDocumentView();
    doc = docView.getDocument();
    selectedLayer = docView.getSelectedLayer();
    selector = view.getToolBar().getColorSelector();
    imageRect = new Rectangle(0, 0, doc.getWidth(), doc.getHeight());
  }

  public void dragEnded() {
  }

  public void click(Point2D pos, int button) {}

  protected void updateDocument() {
    docView.getDocument().updateFlattenedCache();
    docView.getCanvas().repaint();
    doc.setSaved(false);
    view.updateTabNames();
    view.getLayerListView().update();
  }

  protected Color getSelectedColor() {
    if (buttons.contains(MouseEvent.BUTTON1))
      return selector.getPrimary();
    if (buttons.contains(MouseEvent.BUTTON3))
      return selector.getSecondary();
    return null;
  }
}

class DragGesture {
  private Point2D start = new Point2D.Double(), last = new Point2D.Double(), current = new Point2D.Double(), end = new Point2D.Double();
  private boolean dragging = false;
  private Set<Integer> buttons = new HashSet<Integer>();

  void start(Point2D start, int button) {
    this.start.setLocation(start);
    this.last.setLocation(start);
    this.current.setLocation(start);
    buttons.add(button);
  }

  void stop(Point2D end) {
    this.end.setLocation(end);
    dragging = false;
  }

  boolean isDragging() {
    return dragging;
  }

  Point2D getStart() {
    return start;
  }

  Point2D getEnd() {
    if (dragging) return null;
    return end;
  }

  void dragTo(Point2D pos) {
    dragging = true;
    last.setLocation(current);
    current.setLocation(pos);
  }

  Point2D getCurrent() {
    return current;
  }

  Point2D getLast() {
    return last;
  }

  Set getButtons() {
    return buttons;
  }

  void pressButton(int button) {
    buttons.add(button);
  }

  void releaseButton(int button) {
    buttons.remove(button);
  }
}

public class MoveAction extends ToolAction {
  BufferedImage original;
  BufferedImage crop;

  MoveAction(View view) {
    super("Move Tool", "move.png", view);
  }
  public void dragStarted() {
    DocumentView docView = view.getSelectedDocumentView();
    Shape selection = docView.getSelection();
    Rectangle bounds = selection.getBounds();

    original = docView.getSelectedLayer().getImage();
    crop = original.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
  }

  public void dragging() {

  }

  public void dragEnded() {
  }
}

public class LassoAction extends SelectAction {
  Path2D.Double selection;

  LassoAction(View view) {
    super("Lasso Tool", "lasso.png", view);

    options.add(new JLabel("Operation: "));
    options.add(SharedOptions.setOperations);
  }
  
  public void dragStarted() {
    super.dragStarted();
    Point2D start = dragState.getStart();
    selection = new Path2D.Double();
    selection.moveTo(start.getX(), start.getY());
  }

  public void dragging() {
    super.initVars();
    if (!dragState.isDragging()) return; //check if just a click

    if(current.distance(last) > 0.01)
      selection.lineTo(current.getX(), current.getY());
    
    Area selectedArea = new Area(selection);

    switch(SharedOptions.setOperations.getSelectedIndex()) {
      case SharedOptions.REPLACE:
        Path2D.Double closedSelection = (Path2D.Double)selection.clone();
        closedSelection.closePath();
        docView.setSelection(closedSelection);
        return;

      case SharedOptions.ADD: 
        selectedArea.add(preSelection);
        break;

      case SharedOptions.SUBTRACT: 
        Area clone = ((Area)preSelection.clone());
        clone.subtract(selectedArea);
        selectedArea = clone;
        break;

      case SharedOptions.INTERSECT:
        selectedArea.intersect(preSelection);
        break;
      
      case SharedOptions.INVERT:
        selectedArea.exclusiveOr(preSelection);
        break;
    }

    docView.setSelection(selectedArea);
  }

  public void dragEnded() {
    Shape selection = docView.getSelection();
    if(!(selection instanceof Area)) docView.setSelection(new Area(selection));
  }

  @Override
  public void click(Point2D pos, int button) {
    docView = view.getSelectedDocumentView();
    docView.setSelection(null);
  }
}

public class PolygonalLassoTool extends SelectAction {
  Path2D.Double selection;

  PolygonalLassoTool(View view) {
    super("Polygonal Lasso Tool", "polygonallasso.png", view);

    options.add(new JLabel("Operation: "));
    options.add(SharedOptions.setOperations);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    selection = null;
    super.actionPerformed(e);
  }

  @Override
  public void dragStarted() {
    if(selection != null) return;
    super.dragStarted();
    Point2D start = dragState.getStart();
    selection = new Path2D.Double();
    selection.moveTo(start.getX(), start.getY());
    view.getSelectedDocumentView().setSelection(null);
  }

  @Override
  public void dragging() {
    super.initVars();
    Path2D.Double selection2 = selection;
    if (!pressedKeys.contains(KeyEvent.VK_ALT)) selection2 = (Path2D.Double)selection.clone();

    if(current.distance(last) > 0.01)
      selection2.lineTo(current.getX(), current.getY());

    docView.setSelection(selection2);
  }

  public void dragEnded() {
    click(current, buttons.contains(MouseEvent.BUTTON3) ? MouseEvent.BUTTON3: MouseEvent.BUTTON1);
  }

  @Override
  public void click(Point2D pos, int button) {
    DocumentView docView = view.getSelectedDocumentView();
    selection.lineTo(pos.getX(), pos.getY());

    if(SharedOptions.setOperations.getSelectedIndex() == REPLACE && button != MouseEvent.BUTTON3) {
      docView.setSelection(selection);
      return;
    }

    Area selectedArea = new Area(selection);

    switch(SharedOptions.setOperations.getSelectedIndex()) {
      case SharedOptions.ADD: 
        selectedArea.add(preSelection);
        break;

      case SharedOptions.SUBTRACT: 
        Area clone = ((Area)preSelection.clone());
        clone.subtract(selectedArea);
        selectedArea = clone;
        break;

      case SharedOptions.INTERSECT:
        selectedArea.intersect(preSelection);
        break;
      
      case SharedOptions.INVERT:
        selectedArea.exclusiveOr(preSelection);
        break;
    }

    docView.setSelection(selectedArea);
  }
}

public class SelectAction extends ToolAction {
  protected Area preSelection;

  SelectAction(View view) {
    super("Rectangle Select Tool", "select.png", view);

    options.add(new JLabel("Operation: "));
    options.add(SharedOptions.setOperations);
  }

  SelectAction(String name, String toolIconName, View view) {
    //used for crop tool
    super(name, toolIconName, view);
  }

  public void dragStarted() {
    Shape selection = view.getSelectedDocumentView().getSelection();
    preSelection = selection instanceof Area ? (Area)selection : new Area(selection);
  }

  public void dragging() {
    super.initVars();
    if (!dragState.isDragging()) return; //check if just a click

    int startX = (int) start.getX();
    int startY = (int) start.getY();
    int width = (int)current.getX() - startX;
    int height = (int)current.getY() - startY;

    if(pressedKeys.contains(KeyEvent.VK_SHIFT))
      width = height = Math.min(width, height);

    Area selection = new Area(new Rectangle(
      startX + (width < 0 ? width : 0),
      startY + (height < 0 ? height: 0),
      Math.abs(width),
      Math.abs(height))
      .intersection(imageRect));

    if (height == 0 || width == 0) return;

    if (!(this instanceof CropAction)) {
      switch(SharedOptions.setOperations.getSelectedIndex()) {
        case SharedOptions.ADD:
          selection.add(preSelection);
          break;

        case SharedOptions.SUBTRACT: 
          Area clone = ((Area)preSelection.clone());
          clone.subtract(selection);
          selection = clone;
          break;

        case SharedOptions.INTERSECT:
          selection.intersect(preSelection);
          break;

        case SharedOptions.INVERT:
          selection.exclusiveOr(preSelection);
          break;
      }
    }

    docView.setSelection(selection);
  }

  @Override
  public void click(Point2D pos, int button) {
    if(SharedOptions.setOperations.getSelectedIndex() == SharedOptions.REPLACE)
      view.getSelectedDocumentView().setSelection(null);
  }

  @Override
  public String getToolTip() {
    if (docView == null || !docView.hasSelection()) return toolTip;
    Rectangle selection = docView.getSelection().getBounds();
    return String.format(
      "Selection top left: %d, %d. Bounding rectangle size: %d, %d. Area: %d pixels squared",
      selection.x, selection.y, 
      selection.width, selection.height,
      selection.width * selection.height
    );
  }
}

public class CropAction extends SelectAction {
  CropAction(View view) {
    super("Crop Tool", "crop.png", view);
  }

  public void dragStarted() {
    view.getSelectedDocumentView().snapShotManager.save();
  }
  
  public void click(Point2D pos, int button) {}

  public void dragEnded() {
    doc.crop(docView.getSelection().getBounds());
    docView.setSelection(null);
    docView.getCanvas().revalidate();
    docView.updateImageSizeLabel();
    view.getLayerListView().update();
  }
}

public class EyeDropAction extends ToolAction {
  EyeDropAction(View view){
    super("Eyedropper Tool", "eyedrop.png", view);
    options.add(new JLabel("Sampling: "));
    options.add(SharedOptions.sampling);
  }

  public void dragging() {
    super.initVars();
    if (!imageRect.contains(current)) return;

    BufferedImage samplingImage = (String)SharedOptions.sampling.getSelectedItem() == "Image" ? doc.flattened() : selectedLayer.getImage();
    Color c = new Color(samplingImage.getRGB((int) current.getX(), (int) current.getY()), true);

    if (buttons.contains(MouseEvent.BUTTON1))
      selector.setPrimary(c);
    if (buttons.contains(MouseEvent.BUTTON3))
      selector.setSecondary(c);
  }
}

public class BrushAction extends ToolAction {

  BrushAction(View view){
    super("Paintbrush Tool", "brush.png", view);

    options.add(new JLabel("Brush width: "));
    options.add(SharedOptions.weightSpinner);
    options.add(SharedOptions.antialiasingButton);
  }

  public void dragging() {
    super.initVars();
    if (!selectedLayer.isVisible()) return;
    Graphics2D g = selectedLayer.getGraphics();
    g.setClip(docView.getSelection());
    g.setPaint(getSelectedColor());

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
      SharedOptions.antialiasingButton.isSelected() ?
      RenderingHints.VALUE_ANTIALIAS_ON :
      RenderingHints.VALUE_ANTIALIAS_OFF);

    g.setStroke(new BasicStroke(((Double)SharedOptions.weightSpinner.getValue()).floatValue(), BasicStroke.CAP_ROUND, 0));
    g.draw(new Line2D.Double(last.getX(), last.getY(), current.getX(), current.getY()));
    updateDocument();
  }

  public void dragStarted() {
    view.getSelectedDocumentView().snapShotManager.save();
  }
}

public class PencilAction extends ToolAction {
  PencilAction(View view){
    super("Pencil", "pencil.png", view);
  }
  public void dragStarted() {
    view.getSelectedDocumentView().snapShotManager.save();
  }
  public void dragging() {
    super.initVars();
    if (!selectedLayer.isVisible()) return;
    //i should add a commit layer but im running out of time
    Graphics2D g = selectedLayer.getGraphics();
    g.setClip(docView.getSelection());
    g.setPaint(getSelectedColor());
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    g.setStroke(new BasicStroke(1));
    g.drawLine((int)last.getX(), (int)last.getY(), (int)current.getX(), (int)current.getY());
    updateDocument();

  }
}

public class EraserAction extends ToolAction {
  EraserAction(View view){
    super("Eraser", "eraser.png", view);

    options.add(new JLabel("Brush width: "));
    options.add(SharedOptions.weightSpinner);
    options.add(SharedOptions.antialiasingButton);
  }
  public void dragStarted() {
    view.getSelectedDocumentView().snapShotManager.save();
  }
  public void dragging() {
    super.initVars();
    if (!selectedLayer.isVisible()) return;
    Graphics2D g = selectedLayer.getGraphics();

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
      SharedOptions.antialiasingButton.isSelected() ?
      RenderingHints.VALUE_ANTIALIAS_ON :
      RenderingHints.VALUE_ANTIALIAS_OFF);

    g.setClip(docView.getSelection());
    g.setStroke(new BasicStroke(((Double)SharedOptions.weightSpinner.getValue()).floatValue(), BasicStroke.CAP_ROUND, 0));
    Composite before = g.getComposite();
    g.setComposite(AlphaComposite.Clear);
    g.draw(new Line2D.Double(last.getX(), last.getY(), current.getX(), current.getY()));
    g.setComposite(before);

    updateDocument();
  }
}

public class FillAction extends ToolAction {
  FillAction(View view){
    super("Paint Bucket Tool", "fill.png", view);

    options.add(SharedOptions.antialiasingButton);
  }
  public void dragStarted() {
    view.getSelectedDocumentView().snapShotManager.save();
  }
  public void dragging() {
    super.initVars();
    if (!selectedLayer.isVisible()) return;
    if (!docView.getSelection().contains(current)) return;

    Graphics2D g = selectedLayer.getGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
      SharedOptions.antialiasingButton.isSelected() ?
      RenderingHints.VALUE_ANTIALIAS_ON :
      RenderingHints.VALUE_ANTIALIAS_OFF);
    g.setClip(null);
    g.setPaint(getSelectedColor());
    g.fill(docView.getSelection());
    updateDocument();
  }
}

public class TextAction extends ToolAction {
  TextAction(View view){
    super("Text Tool", "text.png", view);
  }

  public void dragging() {
    if (!selectedLayer.isVisible()) return;
  }
}

public class PanAction extends ToolAction {
  PanAction(View view){
    super("Pan Tool", "pan.png", view);
  }

  public void dragging() {
    DocumentView docView = view.getSelectedDocumentView();
    JViewport viewport = docView.getViewport();
    Point2D start = dragState.getStart();
    Point2D current = dragState.getCurrent();
    float scale = docView.getScale();

    Double deltaX = scale * start.getX() - scale * current.getX();
    Double deltaY = scale * start.getY() - scale * current.getY();

    Rectangle view = viewport.getViewRect();
    view.x += Math.round(deltaX);
    view.y += Math.round(deltaY);

    ((JPanel)viewport.getView()).scrollRectToVisible(view);
  }
}

public class ZoomAction extends SelectAction {
  ZoomInAction zoomInAction;
  ZoomOutAction zoomOutAction;
  ZoomToSelectionAction zoomToSelectionAction;
  Shape selection;

  ZoomAction(View view){
    super("Zoom Tool", "zoom.png", view);
    zoomInAction = new ZoomInAction(view);
    zoomOutAction = new ZoomOutAction(view);
    zoomToSelectionAction = new ZoomToSelectionAction(view);
  }

  public void dragStarted() {
    DocumentView docView = view.getSelectedDocumentView();
    this.selection = docView.hasSelection() ? docView.getSelection() : null;
    docView.setSelection(null);
  }

  public void click(Point2D pos, int button) {
    switch(button) {
      case MouseEvent.BUTTON1:
        zoomInAction.setPosition(pos);
        zoomInAction.execute();
        break;
      case MouseEvent.BUTTON3:
        zoomOutAction.setPosition(pos);
        zoomOutAction.execute();
        break;
    }
  }
  public void dragEnded() {
    if (!docView.hasSelection()) return;
    zoomToSelectionAction.execute();
    docView.setSelection(selection);
  }
}
