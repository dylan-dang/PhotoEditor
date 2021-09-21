package controller.actions.menubar;

import java.awt.event.*;
import javax.swing.*;
import view.View;
import view.DocumentView;
import java.io.File;

public abstract class MenuBarAction extends AbstractAction {
    protected View view;
    // private File file;
    // hacky, i know. but i wouldn't be able to reference it in promptFile() and i can't make it
    // final.

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

    protected File promptFile(final boolean isOpen) {
        // final FileChooser fileChooser = new FileChooser();
        // if (isOpen) fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Image
        // Types", "*.png", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif", "*.bmp", ".dib",
        // "*.wbmp", "*.gif"));
        // fileChooser.getExtensionFilters().addAll(
        // new ExtensionFilter("PNG", "*.png"),
        // new ExtensionFilter("JPEG", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif"),
        // new ExtensionFilter("BMP", "*.bmp", ".dib"),
        // new ExtensionFilter("WBMP", "*.wbmp"),
        // new ExtensionFilter("GIF", "*.gif"));
        //// stop the main thread and freeze frame until a file is chosen
        // final CountDownLatch latch = new CountDownLatch(1);
        // Platform.runLater(new Runnable() {
        // @Override
        // public void run() {
        // view.getFrame().setEnabled(false);
        // file = isOpen ? fileChooser.showOpenDialog(null) : fileChooser.showSaveDialog(null);
        // latch.countDown();
        // view.getFrame().setAlwaysOnTop(true);
        // }
        // });
        // try {
        // latch.await();
        // } catch (InterruptedException ex) {
        // throw new RuntimeException(ex);
        // } finally {
        // view.getFrame().setEnabled(true);
        // view.getFrame().setAlwaysOnTop(false);
        // return file;
        // }
        return null;
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
