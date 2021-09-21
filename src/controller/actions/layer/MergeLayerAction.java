package controller.actions.layer;

import java.awt.*;
import java.awt.event.*;

import view.View;
import view.LayerListView;
import model.Layer;
import controller.composites.BlendComposite;

public class MergeLayerAction extends LayerAction {
    public MergeLayerAction(View view) {
        super(view, "Merge Layer Down", "ctrl M");
    }

    public MergeLayerAction(LayerListView layerListView) {
        super("merge.png", layerListView);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        createSnapshot();
        Layer below = doc.getLayers().get(selectedIndex - 1);
        Graphics2D g = below.getGraphics();
        Composite before = g.getComposite();
        BlendComposite blendComposite = selectedLayer.getBlendComposite();
        blendComposite.setOpacity(selectedLayer.getOpacity());
        g.setComposite(blendComposite);
        g.drawImage(selectedLayer.getImage(), null, 0, 0);
        g.setComposite(before); // restore
        layers.remove(selectedLayer);
        docView.setSelectedLayer(below);
        list.update();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && selectedIndex > 0;
    }
}

