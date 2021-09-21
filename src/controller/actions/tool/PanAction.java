package controller.actions.tool;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import view.View;
import view.DocumentView;

public class PanAction extends ToolAction {
    public PanAction(View view) {
        super("Pan Tool", "pan.png", view);
    }

    public void dragging() {
        DocumentView docView = view.getSelectedDocumentView();
        JViewport viewport = docView.getViewport();
        Point2D start = dragState.getStart();
        Point2D current = dragState.getCurrent();
        float scale = docView.getScale();

        Double deltaX = scale * start.getX() - scale * current.getX();
        Double deltaY = scale * start.getY() - scale * current.getY();

        Rectangle view = viewport.getViewRect();
        view.x += Math.round(deltaX);
        view.y += Math.round(deltaY);

        ((JPanel) viewport.getView()).scrollRectToVisible(view);
    }
}

