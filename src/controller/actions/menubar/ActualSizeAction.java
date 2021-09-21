package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class ActualSizeAction extends MenuBarAction {
    public ActualSizeAction(View view) {
        super(view, "Actual Size", "ctrl 0");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        view.getSelectedDocumentView().setScale(1.0f);
    }
}

