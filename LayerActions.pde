public abstract class LayerAction extends AbstractAction {
  protected LayerListView list;
  protected DocumentView docView;
  protected Document doc;
  protected ArrayList layers;
  protected int index;
  LayerAction(String layerActionIconName, LayerListView layerListView) {
    this.list = layerListView;
    putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/layers/actions/%s", layerActionIconName))));
  }
  protected void initVars() {
    docView = list.getView().getSelectedDocumentView();
    doc = docView.getDocument();
    layers = doc.getLayers();
    index = layers.indexOf(docView.getSelectedLayer());
  }
  @Override
  public boolean isEnabled() {
    enabled = list.getView().hasSelectedDocument();
    if(enabled) initVars();
    return enabled;
  }
}

public class AddLayer extends LayerAction {
  AddLayer(LayerListView layerListView) {
    super("add.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    Layer layer = doc.addLayer(index + 1);
    docView.setSelectedLayer(layer);
    list.update();
  }
}

public class RemoveLayer extends LayerAction {
  RemoveLayer(LayerListView layerListView) {
    super("remove.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    layers.remove(index);
    docView.setSelectedLayer((Layer)layers.get(Math.max(index - 1, 0)));
    list.update();
  }
  @Override
  public boolean isEnabled() {
    return super.isEnabled() && layers.size() > 1;
  }
}

public class DuplicateLayer extends LayerAction {
  DuplicateLayer(LayerListView layerListView) {
    super("duplicate.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
  }
  @Override
  public boolean isEnabled() {
    return super.isEnabled() && false;
  }
}

public class MergeLayer extends LayerAction {
  MergeLayer(LayerListView layerListView) {
    super("merge.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    Layer top = docView.getSelectedLayer();
    Layer bottom = doc.getLayers().get(index - 1);

    Graphics2D g = bottom.getGraphics();
    Composite before = g.getComposite();
    BlendComposite blendComposite = top.getBlendComposite();
    blendComposite.setOpacity(top.getOpacity());
    g.setComposite(blendComposite);
    g.drawImage(top.getImage(), null, 0, 0);
    g.setComposite(before);
    layers.remove(top);
    docView.setSelectedLayer(bottom);
    list.update();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && index > 0;
  }
}

public class MoveUpLayer extends LayerAction {
  MoveUpLayer(LayerListView layerListView) {
    super("moveUp.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    int index = layers.indexOf(docView.getSelectedLayer());
    Collections.swap(layers, index, index + 1);
    list.update();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && index + 1 < layers.size();
  }
}

public class MoveDownLayer extends LayerAction {
  MoveDownLayer(LayerListView layerListView) {
    super("moveDown.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    int index = layers.indexOf(docView.getSelectedLayer());
    Collections.swap(layers, index, index - 1);
    list.update();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && index > 0;
  }
}
