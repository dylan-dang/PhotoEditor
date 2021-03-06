public abstract class ToolAction extends AbstractAction {
  View view;

  ToolAction(String toolIconName, View view) {
    this.view = view;
    putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/tools/%s", toolIconName))));
  }
  public void actionPerformed(ActionEvent e) {
    view.setSelectedTool(this);
  }
}

public class MoveAction extends ToolAction {
  MoveAction(View view) {
    super("move.png", view);
  }
}

public class SelectAction extends ToolAction {
  SelectAction(View view) {
    super("select.png", view);
  }
}

public class CropAction extends ToolAction {
  CropAction(View view) {
    super("crop.png", view);
  }
}

public class EyeDropAction extends ToolAction {
  EyeDropAction(View view){
    super("eyedrop.png", view);
  }
}
public class BrushAction extends ToolAction {
  BrushAction(View view){
    super("brush.png", view);
  }
}
public class PencilAction extends ToolAction {
  PencilAction(View view){
    super("pencil.png", view);
  }
}
public class EraserAction extends ToolAction {
  EraserAction(View view){
    super("eraser.png", view);
  }
}
public class FillAction extends ToolAction {
  FillAction(View view){
    super("fill.png", view);
  }
}
public class TextAction extends ToolAction {
  TextAction(View view){
    super("text.png", view);
  }
}
public class PanAction extends ToolAction {
  PanAction(View view){
    super("pan.png", view);
  }
}
public class ZoomAction extends ToolAction {
  ZoomAction(View view){
    super("zoom.png", view);
  }
}
