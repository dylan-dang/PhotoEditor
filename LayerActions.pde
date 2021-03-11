public abstract class LayerAction extends AbstractAction {
  LayerListView layerListView;
  LayerAction(String layerActionIconName, LayerListView layerListView) {
    this.layerListView = layerListView;
    putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/layers/actions/%s", layerActionIconName))));
  }
}

public class AddLayer extends LayerAction {
  AddLayer(LayerListView layerListView) {
    super("add.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }
}

public class RemoveLayer extends LayerAction {
  RemoveLayer(LayerListView layerListView) {
    super("remove.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }
}

public class DuplicateLayer extends LayerAction {
  DuplicateLayer(LayerListView layerListView) {
    super("duplicate.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }
}

public class MergeLayer extends LayerAction {
  MergeLayer(LayerListView layerListView) {
    super("merge.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }
}

public class MoveUpLayer extends LayerAction {
  MoveUpLayer(LayerListView layerListView) {
    super("moveUp.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }
}

public class MoveDownLayer extends LayerAction {
  MoveDownLayer(LayerListView layerListView) {
    super("moveDown.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

  }
}
