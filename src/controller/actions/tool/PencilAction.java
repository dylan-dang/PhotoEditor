package controller.actions.tool;

import java.awt.*;
import view.View;

public class PencilAction extends ToolAction {
    public PencilAction(View view) {
        super("Pencil", "pencil.png", view);
    }

    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }

    public void dragging() {
        super.initVars();
        if (!selectedLayer.isVisible())
            return;
        // i should add a commit layer but im running out of time
        Graphics2D g = selectedLayer.getGraphics();
        g.setClip(docView.getSelection());
        g.setPaint(getSelectedColor());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setStroke(new BasicStroke(1));
        g.drawLine((int) last.getX(), (int) last.getY(), (int) current.getX(),
                (int) current.getY());
        updateDocument();

    }
}

