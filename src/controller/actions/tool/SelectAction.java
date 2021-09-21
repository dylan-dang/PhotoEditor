package controller.actions.tool;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.*;

import javax.swing.*;

import view.View;
import static utils.Constants.SetOps;
import static utils.Constants.setOpsComboBox;;

public class SelectAction extends ToolAction {
    protected Area preSelection;

    public SelectAction(View view) {
        super("Rectangle Select Tool", "select.png", view);

        options.add(new JLabel("Operation: "));
        options.add(setOpsComboBox);
    }

    public SelectAction(String name, String toolIconName, View view) {
        // used for crop tool
        super(name, toolIconName, view);
    }

    public void dragStarted() {
        Shape selection = view.getSelectedDocumentView().getSelection();
        preSelection = selection instanceof Area ? (Area) selection : new Area(selection);
    }

    public void dragging() {
        super.initVars();
        if (!dragState.isDragging())
            return; // check if just a click

        int startX = (int) start.getX();
        int startY = (int) start.getY();
        int width = (int) current.getX() - startX;
        int height = (int) current.getY() - startY;

        if (view.pressedKeys.contains(KeyEvent.VK_SHIFT))
            width = height = Math.min(width, height);

        Area selection = new Area(
                new Rectangle(startX + (width < 0 ? width : 0), startY + (height < 0 ? height : 0),
                        Math.abs(width), Math.abs(height)).intersection(imageRect));

        if (height == 0 || width == 0)
            return;

        if (!(this instanceof CropAction)) {
            switch ((SetOps) setOpsComboBox.getSelectedItem()) {
                case REPLACE:
                    break;

                case ADD:
                    selection.add(preSelection);
                    break;

                case SUBTRACT:
                    Area clone = ((Area) preSelection.clone());
                    clone.subtract(selection);
                    selection = clone;
                    break;

                case INTERSECT:
                    selection.intersect(preSelection);
                    break;

                case INVERT:
                    selection.exclusiveOr(preSelection);
                    break;
            }
        }

        docView.setSelection(selection);
    }

    @Override
    public void click(Point2D pos, int button) {
        if (setOpsComboBox.getSelectedItem() == SetOps.REPLACE)
            view.getSelectedDocumentView().setSelection(null);
    }

    @Override
    public String getToolTip() {
        if (docView == null || !docView.hasSelection())
            return toolTip;
        Rectangle selection = docView.getSelection().getBounds();
        return String.format(
                "Selection top left: %d, %d. Bounding rectangle size: %d, %d. Area: %d pixels squared",
                selection.x, selection.y, selection.width, selection.height,
                selection.width * selection.height);
    }
}

