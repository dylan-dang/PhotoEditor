package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;
import view.View;
import view.DocumentView;

public class FillSelectionAction extends MenuBarAction {
    public FillSelectionAction(View view) {
        super(view, "Fill Selection", "BACK_SPACE");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        Graphics2D g = docView.getSelectedLayer().getGraphics();

        createSnapshot();

        g.setPaint(view.getToolBar().getColorSelector().getPrimary());
        g.fill(docView.getSelection());

        updateDocView();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
    }
}

