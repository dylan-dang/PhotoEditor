package controller.actions.menubar;

import java.awt.event.*;

import view.View;
import model.Document;
import java.io.File;

public class OpenFileAction extends MenuBarAction {
    public OpenFileAction(View view) {
        super(view, "Open...", "ctrl O");
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File file = promptFile(true);
        if (file == null)
            return;
        int index = view.getImageTabs().getSelectedIndex() + 1;
        view.insertDocument(new Document(file), index);
        view.getImageTabs().setSelectedIndex(index);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

