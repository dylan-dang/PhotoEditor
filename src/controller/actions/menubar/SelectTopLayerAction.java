package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class SelectTopLayerAction extends MenuBarAction {
    public SelectTopLayerAction(View view) {
        super(view, "Go to Top Layer", "ctrl alt PAGE_UP");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int count = view.getSelectedDocument().getLayerCount() - 1;
        view.getSelectedDocumentView().setSelectedLayerIndex(count);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView()
                .getSelectedLayerIndex() < view.getSelectedDocument().getLayerCount() - 1;
    }
}

