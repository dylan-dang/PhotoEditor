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

public abstract class ToolAction extends AbstractAction {
  protected String name;
  protected DragGesture dragState = new DragGesture();
  protected Point2D start, last, current;
  protected DocumentView docView;
  protected Document doc;
  protected Set buttons;
  protected Layer selectedLayer;
  protected ColorSelector selector;
  protected Rectangle imageRect;
  private String toolTip;
  View view;

  ToolAction(String name, String toolIconName, View view) {
    this.name = name;
    this.view = view;
    toolTip = name;
    putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/tools/%s", toolIconName))));
  }
  public String getName() {
    return name;
  }
  protected void setToolTip(String toolTip) {
    this.toolTip = toolTip;
  }
  public String getToolTip() {
    return toolTip;
  }

  public ImageIcon getIcon() {
    return (ImageIcon) getValue(Action.SMALL_ICON);
  }

  public void actionPerformed(ActionEvent e) {
    if (view.hasSelectedDocument()) {
      ImageIcon icon = (ImageIcon) getValue(Action.SMALL_ICON);
      view.getSelectedDocumentView().setToolTipIcon(icon);
    }
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

public class MoveAction extends ToolAction {
  MoveAction(View view) {
    super("Move Tool", "move.png", view);
  }

  public void dragging() {

  }

  public void dragEnded() {
  }
}

public class SelectAction extends ToolAction {
  SelectAction(View view) {
    super("Rectangle Select Tool", "select.png", view);
  }

  public void dragging() {
    super.initVars();
    if (!dragState.isDragging()) return; //check if just a click

    int startX = (int) start.getX();
    int startY = (int) start.getY();
    int width = (int)current.getX() - startX + 1;
    int height = (int)current.getY() - startY + 1;

    Rectangle selection = new Rectangle(
      startX + (width <= 0 ? --width : 0),
      startY + (height <= 0 ? --height: 0),
      Math.abs(width),
      Math.abs(height))
      .intersection(imageRect);

    if (selection.height == 0 || selection.width == 0) return;
    docView.setSelection(selection);
  }

  @Override
  public void click(Point2D pos, int button) {
    docView = view.getSelectedDocumentView();
    docView.setSelection(null);
  }
}

public class CropAction extends ToolAction {
  CropAction(View view) {
    super("Crop Tool", "crop.png", view);
  }
  public void dragStarted() {
    view.getSelectedDocumentView().snapShotManager.save();
  }
  public void dragging() {
    super.initVars();
    if (!dragState.isDragging()) return; //check if just a click

    int startX = (int) start.getX();
    int startY = (int) start.getY();
    int width = (int)current.getX() - startX + 1;
    int height = (int)current.getY() - startY + 1;

    Rectangle selection = new Rectangle(
      startX + (width <= 0 ? --width : 0),
      startY + (height <= 0 ? --height: 0),
      Math.abs(width),
      Math.abs(height))
      .intersection(imageRect);

    if (selection.height == 0 || selection.width == 0) return;
    docView.setSelection(selection);
  }
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
  }

  public void dragging() {
    super.initVars();
    if (!imageRect.contains(current)) return;

    BufferedImage samplingImage = selectedLayer.getImage();
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
  }

  public void dragging() {
    super.initVars();
    if (!selectedLayer.isVisible()) return;
    Graphics2D g = selectedLayer.getGraphics();
    g.setClip(docView.getSelection());
    g.setPaint(getSelectedColor());
    g.setStroke(new BasicStroke(100, BasicStroke.CAP_ROUND, 0));
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
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    updateDocument();

  }
}

public class EraserAction extends ToolAction {
  EraserAction(View view){
    super("Eraser", "eraser.png", view);
  }
  public void dragStarted() {
    view.getSelectedDocumentView().snapShotManager.save();
  }
  public void dragging() {
    super.initVars();
    if (!selectedLayer.isVisible()) return;
    Graphics2D g = selectedLayer.getGraphics();
    g.setClip(docView.getSelection());
    g.setStroke(new BasicStroke(100, BasicStroke.CAP_ROUND, 0));
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
  }
  public void dragStarted() {
    view.getSelectedDocumentView().snapShotManager.save();
  }
  public void dragging() {
    super.initVars();
    if (!selectedLayer.isVisible()) return;
    if (!docView.getSelection().contains(current)) return;

    Graphics2D g = selectedLayer.getGraphics();
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

public class ZoomAction extends ToolAction {
  ZoomInAction zoomInAction;
  ZoomOutAction zoomOutAction;
  ZoomAction(View view){
    super("Zoom Tool", "zoom.png", view);
    zoomInAction = new ZoomInAction(view);
    zoomOutAction = new ZoomOutAction(view);
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
}
