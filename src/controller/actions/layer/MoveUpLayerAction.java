package controller.actions.layer;

import java.util.*;
import java.awt.event.*;

import view.View;
import view.LayerListView;

public class MoveUpLayerAction extends LayerAction {
    public MoveUpLayerAction(View view) {
        super(view, "Move Layer Up");
    }

    public MoveUpLayerAction(LayerListView layerListView) {
        super("moveUp.png", layerListView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
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

