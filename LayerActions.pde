public abstract class LayerAction extends MenuBarAction {
  protected LayerListView list;
  protected DocumentView docView;
  protected Document doc;
  protected ArrayList layers;
  protected int selectedIndex;
  protected Layer selectedLayer;

  public LayerAction(View view, String name, Object accelerator) {
    super(view, name, accelerator);
  }

  public LayerAction(View view, String name) {
    super(view, name);
  }

  public LayerAction(String layerActionIconName, LayerListView layerListView) {
    super(null, null);
    this.list = layerListView;
    putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/layers/actions/%s", layerActionIconName))));
  }

  protected void initVars() {
    if (list == null) list = view.getLayerListView();
    docView = view.getSelectedDocumentView();
    doc = docView.getDocument();
    layers = doc.getLayers();
    selectedLayer = docView.getSelectedLayer();
    selectedIndex = docView.getSelectedLayerIndex();
  }

  @Override
  public boolean isEnabled() {
    if (view == null) view = list.getView();
    enabled = view.hasSelectedDocument();
    if(enabled) initVars();
    return enabled;
  }
}

public class AddEmptyLayerAction extends LayerAction {
  AddEmptyLayerAction(View view) {
    super(view, "Add New Layer", "ctrl shift N");
  }

  AddEmptyLayerAction(LayerListView layerListView) {
    super("add.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    createSnapshot();
    Layer layer = doc.addEmptyLayer(++selectedIndex);
    docView.setSelectedLayerIndex(selectedIndex);
    list.update();
  }
}

public class RemoveLayerAction extends LayerAction {
  RemoveLayerAction(View view) {
    super(view, "Delete Layer", "ctrl shift DELETE");
  }

  RemoveLayerAction(LayerListView layerListView) {
    super("remove.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    createSnapshot();
    layers.remove(selectedIndex);
    docView.setSelectedLayer((Layer)layers.get(Math.max(selectedIndex - 1, 0)));
    list.update();
  }
  
  @Override
  public boolean isEnabled() {
    return super.isEnabled() && layers.size() > 1;
  }
}

public class DuplicateLayerAction extends LayerAction {
  DuplicateLayerAction(View view) {
    super(view, "Duplicate Layer", "ctrl shift D");
  }

  DuplicateLayerAction(LayerListView layerListView) {
    super("duplicate.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    createSnapshot();
    Layer copy = selectedLayer.copy();
    doc.addLayer(copy, selectedIndex);
    docView.setSelectedLayerIndex(selectedIndex + 1);
    list.update();
  }
}

public class MergeLayerAction extends LayerAction {
  MergeLayerAction(View view) {
    super(view, "Merge Layer Down", "ctrl M");
  }

  MergeLayerAction(LayerListView layerListView) {
    super("merge.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    createSnapshot();
    Layer below = doc.getLayers().get(selectedIndex - 1);
    Graphics2D g = below.getGraphics();
    Composite before = g.getComposite();
    BlendComposite blendComposite = selectedLayer.getBlendComposite();
    blendComposite.setOpacity(selectedLayer.getOpacity());
    g.setComposite(blendComposite);
    g.drawImage(selectedLayer.getImage(), null, 0, 0);
    g.setComposite(before); //restore
    layers.remove(selectedLayer);
    docView.setSelectedLayer(below);
    list.update();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && selectedIndex > 0;
  }
}

public class MoveUpLayerAction extends LayerAction {
  MoveUpLayerAction(View view) {
    super(view, "Move Layer Up");
  }

  MoveUpLayerAction(LayerListView layerListView) {
    super("moveUp.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    createSnapshot();
    Collections.swap(layers, selectedIndex, selectedIndex + 1);
    docView.setSelectedLayerIndex(selectedIndex + 1);
    list.update();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && selectedIndex + 1 < layers.size();
  }
}

public class MoveDownLayerAction extends LayerAction {
  MoveDownLayerAction(View view) {
    super(view, "Move Layer Down");
  }

  MoveDownLayerAction(LayerListView layerListView) {
    super("moveDown.png", layerListView);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (!isEnabled()) return;
    createSnapshot();
    Collections.swap(layers, selectedIndex, selectedIndex - 1);
    docView.setSelectedLayerIndex(selectedIndex - 1);
    list.update();
  }

  @Override
  public boolean isEnabled() {
    return super.isEnabled() && selectedIndex > 0;
  }
}
