package controller.actions.menubar;

import java.awt.event.*;
import view.View;
import view.DocumentView;

public class SelectLayerAboveAction extends MenuBarAction {
    public SelectLayerAboveAction(View view) {
        super(view, "Go to Layer Above", "alt PAGE_UP");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        DocumentView docView = view.getSelectedDocumentView();

        docView.setSelectedLayerIndex(docView.getSelectedLayerIndex() + 1);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView()
                .getSelectedLayerIndex() < view.getSelectedDocument().getLayerCount() - 1;
    }
}

