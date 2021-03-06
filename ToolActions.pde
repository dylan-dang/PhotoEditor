public abstract class ToolAction extends AbstractAction {
  ToolAction(String toolIconName) {
    putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/tools/%s", toolIconName))));
  }
}
