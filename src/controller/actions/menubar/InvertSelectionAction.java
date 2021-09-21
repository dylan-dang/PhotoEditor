package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import view.View;
import view.DocumentView;

public class InvertSelectionAction extends MenuBarAction {
    public InvertSelectionAction(View view) {
        super(view, "Invert Selection", "ctrl I");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        Dimension size = view.getSelectedDocument().getDimension();
        Area selection = new Area(new Rectangle(0, 0, size.width, size.height));

        selection.exclusiveOr(new Area(docView.getSelection()));

        docView.setSelection(selection);
    }
}

