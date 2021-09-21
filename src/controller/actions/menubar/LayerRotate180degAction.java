package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class LayerRotate180degAction extends MenuBarAction {
    public LayerRotate180degAction(View view) {
        super(view, "Rotate 180°");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        createSnapshot();
        view.getSelectedDocumentView().getSelectedLayer().rotate180deg();
        updateDocView();
    }
}

