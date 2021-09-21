package controller.actions.layer;

import java.awt.event.*;
import view.LayerListView;
import view.View;

public class AddEmptyLayerAction extends LayerAction {
    public AddEmptyLayerAction(View view) {
        super(view, "Add New Layer", "ctrl shift N");
    }

    public AddEmptyLayerAction(LayerListView layerListView) {
        super("add.png", layerListView);
    }

    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        createSnapshot();
        doc.addEmptyLayer(++selectedIndex);
        docView.setSelectedLayerIndex(selectedIndex);
        list.update();
    }
}

