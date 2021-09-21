package controller.actions.menubar;


import java.awt.event.*;
import view.View;

public class LayerFlipHorizontalAction extends MenuBarAction {
    public LayerFlipHorizontalAction(View view) {
        super(view, "Flip Horizontal");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        createSnapshot();
        view.getSelectedDocumentView().getSelectedLayer().flipHorizontally();
        updateDocView();
    }
}

