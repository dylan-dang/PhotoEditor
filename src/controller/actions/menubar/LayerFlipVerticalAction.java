package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class LayerFlipVerticalAction extends MenuBarAction {
    public LayerFlipVerticalAction(View view) {
        super(view, "Flip Vertical");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        createSnapshot();
        view.getSelectedDocumentView().getSelectedLayer().flipVertically();
        updateDocView();
    }
}

