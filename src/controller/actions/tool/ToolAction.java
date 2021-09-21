package controller.actions.tool;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

import javax.swing.*;

import view.DocumentView;
import model.Document;
import model.Layer;
import view.ColorSelector;
import view.View;
import view.ToolOptions;

public abstract class ToolAction extends AbstractAction {
    protected String name;
    protected ArrayList<JComponent> options = new ArrayList<JComponent>();
    protected DragGesture dragState = new DragGesture();
    protected Point2D start, last, current;
    protected DocumentView docView;
    protected Document doc;
    protected Set<Integer> buttons;
    protected Layer selectedLayer;
    protected ColorSelector selector;
    protected Rectangle imageRect;
    protected String toolTip;
    protected ToolOptions toolOptions;
    View view;

    ToolAction(String name, String toolIconName, View view) {
        this.name = name;
        this.view = view;
        this.toolOptions = view.getToolOptions();
        toolTip = name;
        putValue(Action.SMALL_ICON,
                new ImageIcon(String.format("resources/tools/%s", toolIconName)));
    }

    public String getName() {
        return name;
    }

    public String getToolTip() {
        return toolTip;
    }

    public ImageIcon getIcon() {
        return (ImageIcon) getValue(Action.SMALL_ICON);
    }

    public void actionPerformed(ActionEvent e) {
        if (!view.hasSelectedDocument())
            return;
        DocumentView docView = view.getSelectedDocumentView();
        docView.setToolTipIcon((ImageIcon) getValue(Action.SMALL_ICON));
        docView.setToolTipText(toolTip);
    }

    public ArrayList<JComponent> getOptions() {
        return options;
    }

    public DragGesture getDragState() {
        return dragState;
    }

    public void dragStarted() {}

    public void dragging() {}

    public void initVars() {
        start = dragState.getStart();
        current = dragState.getCurrent();
        last = dragState.getLast();
        buttons = dragState.getButtons();
        docView = view.getSelectedDocumentView();
        doc = docView.getDocument();
        selectedLayer = docView.getSelectedLayer();
        selector = view.getToolBar().getColorSelector();
        imageRect = new Rectangle(0, 0, doc.getWidth(), doc.getHeight());
    }

    public void dragEnded() {}

    public void click(Point2D pos, int button) {}

    protected void updateDocument() {
        docView.getDocument().updateFlattenedCache();
        docView.getCanvas().repaint();
        doc.setSaved(false);
        view.updateTabNames();
        view.getLayerListView().update();
    }

    protected Color getSelectedColor() {
        if (buttons.contains(MouseEvent.BUTTON1))
            return selector.getPrimary();
        if (buttons.contains(MouseEvent.BUTTON3))
            return selector.getSecondary();
        return null;
    }
}
