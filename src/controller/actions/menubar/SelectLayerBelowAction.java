package controller.actions.menubar;

import java.awt.event.*;

import view.View;
import view.DocumentView;

public class SelectLayerBelowAction extends MenuBarAction {
    public SelectLayerBelowAction(View view) {
        super(view, "Go to Layer Below", "alt PAGE_DOWN");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled())
            return;
        DocumentView docView = view.getSelectedDocumentView();

        docView.setSelectedLayerIndex(docView.getSelectedLayerIndex() - 1);
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().getSelectedLayerIndex() > 0;
    }
}

