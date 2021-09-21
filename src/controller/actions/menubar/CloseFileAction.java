package controller.actions.menubar;


import java.awt.event.*;
import javax.swing.*;

import view.View;
import view.DocumentView;


public class CloseFileAction extends MenuBarAction {
    private final String[] options = {"Save", "Don't Save", "Cancel"};
    private final int SAVE = 0, DONT_SAVE = 1, CANCEL = 2;
    private int index;
    private int response = 2;

    public CloseFileAction(View view, int index) {
        super(view, "Close", "ctrl W");
        this.index = index;
    }

    public CloseFileAction(View view) {
        this(view, -1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int i = index == -1 ? view.getImageTabs().getSelectedIndex() : index;
        JTabbedPane imageTabs = view.getImageTabs();
        DocumentView docView = (DocumentView) imageTabs.getComponentAt(i);
        if (!docView.getDocument().isSaved()) {
            response = SAVE;
            return;
        }
        response = JOptionPane.showOptionDialog(view.getFrame(),
                String.format("Save changes to \"%s\" before closing?",
                        docView.getDocument().getName()),
                "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null,
                options, options[SAVE]);
        switch (response) {
            case CANCEL:
                return;
            case SAVE:
                new SaveAction(view, docView.getDocument()).execute();
            case DONT_SAVE:
                imageTabs.remove(i);
        }
    }

    public boolean getSuccess() {
        return response != CANCEL;
    }
}

