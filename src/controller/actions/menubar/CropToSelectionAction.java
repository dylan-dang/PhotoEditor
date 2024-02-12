package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Area;

import model.Layer;
import view.View;
import model.Document;
import view.DocumentView;

public class CropToSelectionAction extends MenuBarAction {
    public CropToSelectionAction(View view) {
        super(view, "Crop to Selection", "ctrl shift X");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Document doc = view.getSelectedDocument();
        DocumentView docView = view.getSelectedDocumentView();

        createSnapshot();

        Shape selection = docView.getSelection();
        Rectangle bounds = selection.getBounds();
        Area clearArea = new Area(bounds);
        clearArea.exclusiveOr(new Area(selection));
        for (Layer layer : doc.getLayers()) {
            Graphics2D g = layer.getGraphics();
            g.setClip(clearArea);
            Composite before = g.getComposite();
            g.setComposite(AlphaComposite.Clear);
            g.fill(clearArea);
            g.setComposite(before);
            g.dispose();
        }

        doc.crop(bounds);
        docView.setSelection(null);

        updateDocView();
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
    }
}

