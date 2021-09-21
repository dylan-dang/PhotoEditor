package controller.actions.tool;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import view.View;
import static utils.Constants.weightSpinner;
import static utils.Constants.antialiasingButton;

public class BrushAction extends ToolAction {

    public BrushAction(View view) {
        super("Paintbrush Tool", "brush.png", view);

        options.add(new JLabel("Brush width: "));
        options.add(weightSpinner);
        options.add(antialiasingButton);
    }

    public void dragging() {
        super.initVars();
        if (!selectedLayer.isVisible())
            return;
        Graphics2D g = selectedLayer.getGraphics();
        g.setClip(docView.getSelection());
        g.setPaint(getSelectedColor());

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                antialiasingButton.isSelected() ? RenderingHints.VALUE_ANTIALIAS_ON
                        : RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setStroke(new BasicStroke(((Double) weightSpinner.getValue()).floatValue(),
                BasicStroke.CAP_ROUND, 0));
        g.draw(new Line2D.Double(last.getX(), last.getY(), current.getX(), current.getY()));
        updateDocument();
    }

    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }
}

