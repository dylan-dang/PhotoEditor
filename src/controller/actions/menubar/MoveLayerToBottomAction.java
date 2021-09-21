package controller.actions.menubar;

import java.util.*;
import java.awt.event.*;
import view.View;
import view.DocumentView;
import model.Layer;

public class MoveLayerToBottomAction extends MenuBarAction {
    public MoveLayerToBottomAction(View view) {
        super(view, "Move Layer to Bottom");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

        createSnapshot();
        layers.add(0, layers.remove(docView.getSelectedLayerIndex()));
        docView.setSelectedLayerIndex(0);

        updateDocView();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().getSelectedLayerIndex() > 0;
    }
}
