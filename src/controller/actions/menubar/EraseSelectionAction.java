package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;
import view.View;
import view.DocumentView;

public class EraseSelectionAction extends MenuBarAction {
    public EraseSelectionAction(View view) {
        super(view, "Erase Selection", "DELETE");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        // Rectangle selected = docView.getSelection().getBounds();
        Graphics2D g = docView.getSelectedLayer().getGraphics();

        createSnapshot();

        Composite before = g.getComposite();
        g.setComposite(AlphaComposite.Clear);
        g.fill(docView.getSelection());
        g.setComposite(before);

        view.getSelectedDocumentView().setSelection(null);
        updateDocView();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
    }
}

