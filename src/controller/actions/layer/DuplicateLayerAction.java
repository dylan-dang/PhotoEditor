package controller.actions.layer;

import java.awt.event.*;
import view.View;
import view.LayerListView;
import model.Layer;

public class DuplicateLayerAction extends LayerAction {
    public DuplicateLayerAction(View view) {
        super(view, "Duplicate Layer", "ctrl shift D");
    }

    public DuplicateLayerAction(LayerListView layerListView) {
        super("duplicate.png", layerListView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        createSnapshot();
        Layer copy = selectedLayer.copy();
        doc.addLayer(copy, selectedIndex);
        docView.setSelectedLayerIndex(selectedIndex + 1);
        list.update();
    }
}

