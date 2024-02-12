package controller.actions.menubar;

import java.awt.event.*;
import javax.swing.*;
import view.View;
import view.DocumentView;

public abstract class MenuBarAction extends AbstractAction {
    protected View view;

    public MenuBarAction(View view, String name, Object accelerator) {
        this.view = view;
        putValue(NAME, name);
        KeyStroke acc = null;
        if (accelerator instanceof KeyStroke)
            acc = (KeyStroke) accelerator;
        if (accelerator instanceof String)
            acc = KeyStroke.getKeyStroke((String) accelerator);
        if (acc == null)
            return;
        putValue(ACCELERATOR_KEY, acc);
    }

    public MenuBarAction(View view, String name) {
        this(view, name, null);
    }

    public void execute() {
        actionPerformed(new ActionEvent(view.getFrame(), ActionEvent.ACTION_FIRST, null));
    }

    @Override
    public boolean isEnabled() {
        return view.hasSelectedDocument();
    }

    protected void updateDocView() {
        DocumentView docView = view.getSelectedDocumentView();
        docView.getCanvas().revalidate();
        docView.updateImageSizeLabel();
        view.getLayerListView().update();
    }

    protected void createSnapshot() {
        view.getSelectedDocumentView().snapShotManager.save();
    }
}

// File Menu Actions
