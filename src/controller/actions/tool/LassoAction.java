package controller.actions.tool;

import java.awt.*;
import java.awt.geom.*;

import javax.swing.*;
import view.View;
import static utils.Constants.SetOps;
import static utils.Constants.setOpsComboBox;

public class LassoAction extends SelectAction {
    private Path2D.Double selection;

    public LassoAction(View view) {
        super("Lasso Tool", "lasso.png", view);

        options.add(new JLabel("Operation: "));
        options.add(setOpsComboBox);
    }

    public void dragStarted() {
        super.dragStarted();
        Point2D start = dragState.getStart();
        selection = new Path2D.Double();
        selection.moveTo(start.getX(), start.getY());
    }

    public void dragging() {
        super.initVars();
        if (!dragState.isDragging())
            return; // check if just a click

        if (current.distance(last) > 0.01)
            selection.lineTo(current.getX(), current.getY());

        Area selectedArea = new Area(selection);

        switch ((SetOps) setOpsComboBox.getSelectedItem()) {
            case REPLACE:
                Path2D.Double closedSelection = (Path2D.Double) selection.clone();
                closedSelection.closePath();
                docView.setSelection(closedSelection);
                return;

            case ADD:
                selectedArea.add(preSelection);
                break;

            case SUBTRACT:
                Area clone = ((Area) preSelection.clone());
                clone.subtract(selectedArea);
                selectedArea = clone;
                break;

            case INTERSECT:
                selectedArea.intersect(preSelection);
                break;

            case INVERT:
                selectedArea.exclusiveOr(preSelection);
                break;
        }

        docView.setSelection(selectedArea);
    }

    public void dragEnded() {
        Shape selection = docView.getSelection();
        if (!(selection instanceof Area))
            docView.setSelection(new Area(selection));
    }

    @Override
    public void click(Point2D pos, int button) {
        docView = view.getSelectedDocumentView();
        docView.setSelection(null);
    }
}

