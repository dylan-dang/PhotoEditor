package controller.actions.tool;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import controller.actions.menubar.*;
import view.View;
import view.DocumentView;

public class ZoomAction extends SelectAction {
    ZoomInAction zoomInAction;
    ZoomOutAction zoomOutAction;
    ZoomToSelectionAction zoomToSelectionAction;
    Shape selection;

    public ZoomAction(View view) {
        super("Zoom Tool", "zoom.png", view);
        zoomInAction = new ZoomInAction(view);
        zoomOutAction = new ZoomOutAction(view);
        zoomToSelectionAction = new ZoomToSelectionAction(view);
    }

    public void dragStarted() {
        DocumentView docView = view.getSelectedDocumentView();
        this.selection = docView.hasSelection() ? docView.getSelection() : null;
        docView.setSelection(null);
    }

    public void click(Point2D pos, int button) {
        switch (button) {
            case MouseEvent.BUTTON1:
                zoomInAction.setPosition(pos);
                zoomInAction.execute();
                break;
            case MouseEvent.BUTTON3:
                zoomOutAction.setPosition(pos);
                zoomOutAction.execute();
                break;
        }
    }

    public void dragEnded() {
        if (!docView.hasSelection())
            return;
        zoomToSelectionAction.execute();
        docView.setSelection(selection);
    }
}
