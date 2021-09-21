package controller.actions.menubar;

import java.util.*;
import java.awt.event.*;
import view.View;
import view.DocumentView;
import model.Layer;

public class MoveLayerToTopAction extends MenuBarAction {
    public MoveLayerToTopAction(View view) {
        super(view, "Move Layer to Top");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

        createSnapshot();
        layers.add(layers.remove(docView.getSelectedLayerIndex()));
        docView.setSelectedLayerIndex(layers.size() - 1);

        updateDocView();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView()
                .getSelectedLayerIndex() < view.getSelectedDocument().getLayerCount() - 1;
    }
}

