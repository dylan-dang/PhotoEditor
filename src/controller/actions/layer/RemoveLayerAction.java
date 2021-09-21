package controller.actions.layer;

import java.awt.event.*;
import view.View;
import view.LayerListView;
import model.Layer;

public class RemoveLayerAction extends LayerAction {
    public RemoveLayerAction(View view) {
        super(view, "Delete Layer", "ctrl shift DELETE");
    }

    public RemoveLayerAction(LayerListView layerListView) {
        super("remove.png", layerListView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        createSnapshot();
        layers.remove(selectedIndex);
        docView.setSelectedLayer((Layer) layers.get(Math.max(selectedIndex - 1, 0)));
        list.update();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && layers.size() > 1;
    }
}

