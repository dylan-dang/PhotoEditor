package controller.actions.tool;

import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

import javax.swing.*;

import view.View;
import view.DocumentView;
import static utils.Constants.setOpsComboBox;
import static utils.Constants.SetOps;

public class PolygonalLassoTool extends SelectAction {
    private Path2D.Double selection;

    public PolygonalLassoTool(View view) {
        super("Polygonal Lasso Tool", "polygonallasso.png", view);

        options.add(new JLabel("Operation: "));
        options.add(setOpsComboBox);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        selection = null;
        super.actionPerformed(e);
    }

    @Override
    public void dragStarted() {
        if (selection != null)
            return;
        super.dragStarted();
        Point2D start = dragState.getStart();
        selection = new Path2D.Double();
        selection.moveTo(start.getX(), start.getY());
        view.getSelectedDocumentView().setSelection(null);
    }

    @Override
    public void dragging() {
        super.initVars();
        Path2D.Double selection2 = selection;
        if (!view.pressedKeys.contains(KeyEvent.VK_ALT))
            selection2 = (Path2D.Double) selection.clone();

        if (current.distance(last) > 0.01)
            selection2.lineTo(current.getX(), current.getY());

        docView.setSelection(selection2);
    }

    public void dragEnded() {
        click(current,
                buttons.contains(MouseEvent.BUTTON3) ? MouseEvent.BUTTON3 : MouseEvent.BUTTON1);
    }

    @Override
    public void click(Point2D pos, int button) {
        DocumentView docView = view.getSelectedDocumentView();
        selection.lineTo(pos.getX(), pos.getY());

        if (setOpsComboBox.getSelectedItem() == SetOps.REPLACE && button != MouseEvent.BUTTON3) {
            docView.setSelection(selection);
            return;
        }

        Area selectedArea = new Area(selection);

        switch ((SetOps) setOpsComboBox.getSelectedItem()) {
            case REPLACE:
                break;

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
}

