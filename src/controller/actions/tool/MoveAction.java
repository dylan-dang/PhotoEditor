package controller.actions.tool;

import java.awt.*;
import java.awt.image.*;
import view.View;
import view.DocumentView;

public class MoveAction extends ToolAction {
    BufferedImage original;
    BufferedImage crop;

    MoveAction(View view) {
        super("Move Tool", "move.png", view);
    }

    public void dragStarted() {
        DocumentView docView = view.getSelectedDocumentView();
        Shape selection = docView.getSelection();
        Rectangle bounds = selection.getBounds();

        original = docView.getSelectedLayer().getImage();
        crop = original.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void dragging() {

    }

    public void dragEnded() {}
}

