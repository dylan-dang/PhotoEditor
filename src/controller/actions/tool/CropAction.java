package controller.actions.tool;

import java.awt.geom.*;
import view.View;

public class CropAction extends SelectAction {
    public CropAction(View view) {
        super("Crop Tool", "crop.png", view);
    }

    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }

    public void click(Point2D pos, int button) {}

    public void dragEnded() {
        doc.crop(docView.getSelection().getBounds());
        docView.setSelection(null);
        docView.getCanvas().revalidate();
        docView.updateImageSizeLabel();
        view.getLayerListView().update();
    }
}

