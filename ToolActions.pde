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
  protected DragGesture dragState = new DragGesture();

  protected Point2D start, last, current;
  protected DocumentView docView;
  protected Document doc;
  protected Set buttons;
  protected Layer selectedLayer;
  protected ColorSelector selector;
  protected Rectangle2D.Double imageRect;

  View view;
  ToolAction(String toolIconName, View view) {
    this.view = view;
    putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/tools/%s", toolIconName))));
  }
  public void actionPerformed(ActionEvent e) {}
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
    imageRect = new Rectangle2D.Double(0, 0, doc.getWidth(), doc.getHeight());
  };
  public void dragEnded() {}
  public void click(Point2D pos, int button) {}
  protected void updateDocument() {
    docView.getDocument().updateFlattenedView();
    docView.getCanvas().repaint();
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
    super("move.png", view);
  }
  public void dragging() {

  }
}

public class SelectAction extends ToolAction {
  SelectAction(View view) {
    super("select.png", view);
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
      .intersection(new Rectangle(0, 0, doc.getWidth(), doc.getHeight()));

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
    super("crop.png", view);
  }
  public void dragging() {

  }
}

public class EyeDropAction extends ToolAction {
  EyeDropAction(View view){
    super("eyedrop.png", view);
  }
  public void dragging() {
    super.initVars();
    if (!imageRect.contains(current)) return;

    BufferedImage samplingImage = selectedLayer.getImage();
    int c = samplingImage.getRGB((int) current.getX(), (int) current.getY());

    if (buttons.contains(MouseEvent.BUTTON1))
      selector.setPrimary(c);
    if (buttons.contains(MouseEvent.BUTTON3))
      selector.setSecondary(c);
  }
}

public class BrushAction extends ToolAction {
  BrushAction(View view){
    super("brush.png", view);
  }
  public void dragging() {
    super.initVars();
    if (!imageRect.contains(current)) return;
    Color c = getSelectedColor();

    Stroke stroke = new BasicStroke(100, BasicStroke.CAP_ROUND, 0);
    selectedLayer.brush(last.getX(), last.getY(), current.getX(), current.getY(), stroke, c);
    updateDocument();
  }
}

public class PencilAction extends ToolAction {
  PencilAction(View view){
    super("pencil.png", view);
  }
  public void dragging() {
    super.initVars();

    if (!imageRect.contains(current)) return;
    Color c = getSelectedColor();
    //i should change this to polyline with a commit layer but im running out of time
    selectedLayer.drawLine((int)last.getX(), (int)last.getY(), (int)current.getX(), (int)current.getY(), c);
    updateDocument();
  }
}

public class EraserAction extends ToolAction {
  EraserAction(View view){
    super("eraser.png", view);
  }
  public void dragging() {
    super.initVars();
    if (!imageRect.contains(current)) return;
    Color c = getSelectedColor();

    Stroke stroke = new BasicStroke(100, BasicStroke.CAP_ROUND, 0);
    selectedLayer.erase(last.getX(), last.getY(), current.getX(), current.getY(), stroke);
    updateDocument();
  }
}

public class FillAction extends ToolAction {
  FillAction(View view){
    super("fill.png", view);
  }
  public void dragging() {

  }
}

public class TextAction extends ToolAction {
  TextAction(View view){
    super("text.png", view);
  }
  public void dragging() {

  }
}

public class PanAction extends ToolAction {
  PanAction(View view){
    super("pan.png", view);
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
  ZoomAction(View view){
    super("zoom.png", view);
  }
  public void click(Point2D pos, int button) {
    DocumentView docView = view.getSelectedDocumentView();
    final float[] ZOOM_TABLE = docView.ZOOM_TABLE;
    int previousIndex = 0;
    float scale = docView.getScale();

    for(int i = 0; i < ZOOM_TABLE.length; i++) {
      if (ZOOM_TABLE[i] <= scale * 100) {
        if (button == MouseEvent.BUTTON3 && ZOOM_TABLE[i] == scale * 100) continue;
        previousIndex = i;
      }
    }
    pos.setLocation(pos.getX() * scale, pos.getY() * scale);
    try {
      docView.setScale(ZOOM_TABLE[previousIndex + (button == MouseEvent.BUTTON1 ? 1 : 0) - (button == MouseEvent.BUTTON3 ? 1 : 0)] / 100, pos);
    } catch(ArrayIndexOutOfBoundsException e) {} // too lazy to check is out of bounds, plus it's probably faster
  }
}
