package controller.actions.tool;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

import view.View;
import static utils.Constants.weightSpinner;
import static utils.Constants.antialiasingButton;


public class EraserAction extends ToolAction {
    public EraserAction(View view) {
        super("Eraser", "eraser.png", view);

        options.add(new JLabel("Brush width: "));
        options.add(weightSpinner);
        options.add(antialiasingButton);
    }

    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }

    public void dragging() {
        super.initVars();
        Graphics2D g = selectedLayer.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                antialiasingButton.isSelected() ? RenderingHints.VALUE_ANTIALIAS_ON
                        : RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setClip(docView.getSelection());
        g.setStroke(new BasicStroke(((Double) weightSpinner.getValue()).floatValue(),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER));
        Composite before = g.getComposite();
        g.setComposite(AlphaComposite.Clear);
        g.draw(new Line2D.Double(last.getX(), last.getY(), current.getX(), current.getY()));
        g.setComposite(before);

        updateDocument();
    }
}

