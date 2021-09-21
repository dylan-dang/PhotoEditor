package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class SelectBottomLayerAction extends MenuBarAction {
    public SelectBottomLayerAction(View view) {
        super(view, "Go to Bottom Layer", "ctrl alt PAGE_DOWN");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        view.getSelectedDocumentView().setSelectedLayerIndex(0);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().getSelectedLayerIndex() > 0;
    }
}

