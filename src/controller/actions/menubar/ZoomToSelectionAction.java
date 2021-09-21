package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;

import view.View;
import view.DocumentView;

public class ZoomToSelectionAction extends MenuBarAction {
    public ZoomToSelectionAction(View view) {
        super(view, "Zoom to Selection", "ctrl shift B");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        Rectangle selected = docView.getSelection().getBounds();
        Dimension extentSize = docView.getViewport().getExtentSize();
        docView.setScale(Math.min((float) extentSize.width / (float) selected.width,
                (float) extentSize.height / (float) selected.height));

        Rectangle scaledSelection = docView.getScaledSelection().getBounds();
        docView.getViewport()
                .setViewPosition(new Point(
                        scaledSelection.x - (extentSize.width - scaledSelection.width) / 2,
                        scaledSelection.y - (extentSize.height - scaledSelection.height) / 2));
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
    }
}

