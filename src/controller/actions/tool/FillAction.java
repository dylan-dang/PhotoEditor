package controller.actions.tool;

import java.awt.*;
import view.View;
import static utils.Constants.antialiasingButton;


public class FillAction extends ToolAction {
    public FillAction(View view) {
        super("Paint Bucket Tool", "fill.png", view);

        options.add(antialiasingButton);
    }

    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }

    public void dragging() {
        super.initVars();
        if (!selectedLayer.isVisible())
            return;
        if (!docView.getSelection().contains(current))
            return;

        Graphics2D g = selectedLayer.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                antialiasingButton.isSelected() ? RenderingHints.VALUE_ANTIALIAS_ON
                        : RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setClip(null);
        g.setPaint(getSelectedColor());
        g.fill(docView.getSelection());
        updateDocument();
    }
}

