package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;

import view.View;
import model.Document;
import view.DocumentView;

public class ZoomToWindowAction extends MenuBarAction {
    public ZoomToWindowAction(View view) {
        super(view, "Zoom to Window", "ctrl B");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        Document doc = docView.getDocument();
        Dimension extentSize = docView.getViewport().getExtentSize();
        docView.setScale(Math.min((float) extentSize.width / doc.getWidth(),
                (float) extentSize.height / doc.getHeight()));
    }
}

