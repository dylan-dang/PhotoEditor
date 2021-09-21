package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class RedoAction extends MenuBarAction {
    public RedoAction(View view) {
        super(view, "Redo", "ctrl shift Z");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        view.getSelectedDocumentView().getSnapShotManager().redo();
        updateDocView();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled()
                && view.getSelectedDocumentView().getSnapShotManager().ableToRedo();
    }
}

