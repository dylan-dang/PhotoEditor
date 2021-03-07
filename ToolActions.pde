class DragGesture {
  private Point2D start, last, current, end;
  private boolean dragging = false;
  private Set<Integer> buttons = new HashSet<Integer>();
  void start(Point2D start, int button) {
    last = current = this.start = start;
    dragging = true;
    buttons.add(button);
  }
  void stop(Point2D end) {
    this.end = end;
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
    last = current;
    current = pos;
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
  View view;
  ToolAction(String toolIconName, View view) {
    this.view = view;
    putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/tools/%s", toolIconName))));
  }
  public void actionPerformed(ActionEvent e) {
    view.setSelectedTool(this);
  }
  public DragGesture getDragState() {
    return dragState;
  }
  public abstract void execute();
}

public class MoveAction extends ToolAction {
  MoveAction(View view) {
    super("move.png", view);
  }
  public void execute() {

  }
}

public class SelectAction extends ToolAction {
  SelectAction(View view) {
    super("select.png", view);
  }
  public void execute() {

  }
}

public class CropAction extends ToolAction {
  CropAction(View view) {
    super("crop.png", view);
  }
  public void execute() {

  }
}

public class EyeDropAction extends ToolAction {
  EyeDropAction(View view){
    super("eyedrop.png", view);
  }
  public void execute() {
    Point2D pos = dragState.getCurrent();
    BufferedImage image = view.getSelectedDocument().getFlattenedView();
    int c = image.getRGB((int) pos.getX(), (int) pos.getY());
    ColorSelector selector = view.selector;
    Set buttons = dragState.getButtons();
    if (buttons.contains(MouseEvent.BUTTON1)) {
      selector.setPrimary(c);
    }
    if (buttons.contains(MouseEvent.BUTTON3)) {
      selector.setSecondary(c);
    }
  }
}
public class BrushAction extends ToolAction {
  BrushAction(View view){
    super("brush.png", view);
  }
  public void execute() {

  }
}
public class PencilAction extends ToolAction {
  PencilAction(View view){
    super("pencil.png", view);
  }
  public void execute() {

  }
}
public class EraserAction extends ToolAction {
  EraserAction(View view){
    super("eraser.png", view);
  }
  public void execute() {

  }
}
public class FillAction extends ToolAction {
  FillAction(View view){
    super("fill.png", view);
  }
  public void execute() {

  }
}
public class TextAction extends ToolAction {
  TextAction(View view){
    super("text.png", view);
  }
  public void execute() {

  }
}
public class PanAction extends ToolAction {
  PanAction(View view){
    super("pan.png", view);
  }
  public void execute() {

  }
}
public class ZoomAction extends ToolAction {
  ZoomAction(View view){
    super("zoom.png", view);
  }
  public void execute() {

  }
}
