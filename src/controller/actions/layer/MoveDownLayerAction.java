package controller.actions.layer;

import java.util.*;
import java.awt.event.*;

import view.View;
import view.LayerListView;

public class MoveDownLayerAction extends LayerAction {
    public MoveDownLayerAction(View view) {
        super(view, "Move Layer Down");
    }

    public MoveDownLayerAction(LayerListView layerListView) {
        super("moveDown.png", layerListView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
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
