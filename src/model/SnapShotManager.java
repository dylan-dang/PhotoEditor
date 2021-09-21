package model;

import java.util.*;

import view.DocumentView;

public class SnapShotManager {
    Document doc;
    DocumentView docView;
    ArrayDeque<SnapShot> undoHistory = new ArrayDeque<SnapShot>();
    ArrayDeque<SnapShot> redoHistory = new ArrayDeque<SnapShot>();

    public SnapShotManager(DocumentView docView) {
        this.docView = docView;
        this.doc = docView.getDocument();
    }

    public void save(SnapShot snapshot) {
        redoHistory.clear();
        undoHistory.push(snapshot);
    }

    public void save() {
        redoHistory.clear();
        undoHistory.push(new SnapShot(doc, docView.getSelectedLayerIndex()));
    }

    public void undo() {
        if (!ableToUndo())
            return;
        SnapShot save = undoHistory.pop();
        redoHistory.push(new SnapShot(doc, docView.getSelectedLayerIndex()));
        restore(save);
    }

    public void redo() {
        if (!ableToRedo())
            return;
        SnapShot save = redoHistory.pop();
        undoHistory.push(new SnapShot(doc, docView.getSelectedLayerIndex()));
        restore(save);
    }

    private void restore(SnapShot save) {
        ArrayList<Layer> layers = doc.getLayers();
        layers.clear();
        layers.addAll(save.getLayers());
        doc.setHeight(save.getHeight());
        doc.setWidth(save.getWidth());
        docView.setSelectedLayerIndex(save.getSelectedLayer());
        docView.revalidate();
        docView.repaint();
    }

    public boolean ableToUndo() {
        return !undoHistory.isEmpty();
    }

    public boolean ableToRedo() {
        return !redoHistory.isEmpty();
    }
}
