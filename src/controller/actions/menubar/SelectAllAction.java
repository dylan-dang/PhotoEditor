package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;
import view.View;

public class SelectAllAction extends MenuBarAction {
    public SelectAllAction(View view) {
        super(view, "Select All", "ctrl A");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Dimension size = view.getSelectedDocument().getDimension();
        Rectangle selection = new Rectangle(0, 0, size.width, size.height);
        view.getSelectedDocumentView().setSelection(selection);
    }
}

