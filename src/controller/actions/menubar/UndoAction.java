package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class UndoAction extends MenuBarAction {
    public UndoAction(View view) {
        super(view, "Undo", "ctrl Z");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        view.getSelectedDocumentView().snapShotManager.undo();
        updateDocView();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().snapShotManager.ableToUndo();
    }
}

