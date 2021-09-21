package controller.actions.menubar;

import java.awt.event.*;
import java.awt.geom.*;

import view.View;
import view.DocumentView;

public class ZoomOutAction extends MenuBarAction {
    private Point2D pos;

    public ZoomOutAction(View view) {
        super(view, "Zoom Out", "ctrl MINUS");
    }

    public void setPosition(Point2D pos) {
        this.pos = pos;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        final float[] ZOOM_TABLE = docView.ZOOM_TABLE;
        int previousIndex = 0;
        float scale = docView.getScale();

        for (int i = 0; i < ZOOM_TABLE.length; i++) {
            if (ZOOM_TABLE[i] < scale * 100)
                previousIndex = i;
        }
        if (previousIndex < 1)
            return;
        float newScale = ZOOM_TABLE[previousIndex - 1] / 100;
        if (pos == null) {
            docView.setScale(newScale);
        } else {
            pos.setLocation(pos.getX() * scale, pos.getY() * scale);
            docView.setScale(newScale, pos);
            pos = null;
        }
    }
}

