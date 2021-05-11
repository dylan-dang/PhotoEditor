public abstract class LayerAction extends AbstractAction {
    protected LayerListView list;
    protected DocumentView docView;
    protected Document doc;
    protected ArrayList layers;
    protected int selectedIndex;
    protected Layer selectedLayer;
    
    LayerAction(String layerActionIconName, LayerListView layerListView) {
        this.list = layerListView;
        putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/layers/actions/%s", layerActionIconName))));
    }
    
    protected void initVars() {
        docView = list.getView().getSelectedDocumentView();
        doc = docView.getDocument();
        layers = doc.getLayers();
        selectedLayer = docView.getSelectedLayer();
        selectedIndex = docView.getSelectedLayerIndex();
    }
    
    @Override
    public boolean isEnabled() {
        enabled = list.getView().hasSelectedDocument();
        if (enabled) initVars();
        return enabled;
    }
}

public class addEmptyLayer extends LayerAction {
    addEmptyLayer(LayerListView layerListView) {
        super("add.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
        Layer layer = doc.addEmptyLayer(++selectedIndex);
        docView.setSelectedLayerIndex(selectedIndex);
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
        docView.save();
        layers.remove(selectedIndex);
        docView.setSelectedLayer((Layer)layers.get(Math.max(selectedIndex - 1, 0)));
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
        docView.save();
        Layer copy = selectedLayer.copy();
        doc.addLayer(copy, selectedIndex);
        docView.setSelectedLayerIndex(selectedIndex + 1);
        list.update();
    }
}

public class MergeLayer extends LayerAction {
    MergeLayer(LayerListView layerListView) {
        super("merge.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
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

public class MoveUpLayer extends LayerAction {
    MoveUpLayer(LayerListView layerListView) {
        super("moveUp.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
        Collections.swap(layers, selectedIndex, selectedIndex + 1);
        docView.setSelectedLayerIndex(selectedIndex + 1);
        list.update();
    }
    
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && selectedIndex + 1 < layers.size();
    }
}

public class MoveDownLayer extends LayerAction {
    MoveDownLayer(LayerListView layerListView) {
        super("moveDown.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
        Collections.swap(layers, selectedIndex, selectedIndex - 1);
        docView.setSelectedLayerIndex(selectedIndex - 1);
        list.update();
    }
    
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && selectedIndex > 0;
    }
}
