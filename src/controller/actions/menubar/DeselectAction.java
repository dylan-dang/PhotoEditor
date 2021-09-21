package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class DeselectAction extends MenuBarAction {
    public DeselectAction(View view) {
        super(view, "Deselect", "ctrl D");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        view.getSelectedDocumentView().setSelection(null);
        updateDocView();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
    }
}

// View Menu Actions
