package controller.actions.menubar;

import java.util.*;
import java.awt.event.*;
import java.awt.image.*;
import view.View;
import model.Document;
import model.Layer;

public class FlattenAction extends MenuBarAction {
    public FlattenAction(View view) {
        super(view, "Flatten", "ctrl shift F");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Document document = view.getSelectedDocument();
        ArrayList<Layer> layers = document.getLayers();
        BufferedImage flattened = document.flattened();

        createSnapshot();

        layers.clear();
        layers.add(new Layer(flattened));

        view.getSelectedDocumentView().setSelectedLayerIndex(0);
        view.getLayerListView().update();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocument().getLayerCount() > 1;
    }
}

// Layer Actions

