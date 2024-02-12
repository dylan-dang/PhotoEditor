package controller.actions.layer;

import java.util.*;
import javax.swing.*;

import controller.actions.menubar.*;
import view.LayerListView;
import view.DocumentView;
import model.Document;
import model.Layer;
import view.View;

public abstract class LayerAction extends MenuBarAction {
    protected LayerListView list;
    protected DocumentView docView;
    protected Document doc;
    protected ArrayList<Layer> layers;
    protected int selectedIndex;
    protected Layer selectedLayer;

    public LayerAction(View view, String name, Object accelerator) {
        super(view, name, accelerator);
    }

    public LayerAction(View view, String name) {
        super(view, name);
    }

    public LayerAction(String layerActionIconName, LayerListView layerListView) {
        super(null, null);
        this.list = layerListView;
        putValue(Action.SMALL_ICON,
                new ImageIcon(String.format("assets/layers/actions/%s", layerActionIconName)));
    }

    protected void initVars() {
        if (list == null)
            list = view.getLayerListView();
        docView = view.getSelectedDocumentView();
        doc = docView.getDocument();
        layers = doc.getLayers();
        selectedLayer = docView.getSelectedLayer();
        selectedIndex = docView.getSelectedLayerIndex();
    }

    @Override
    public boolean isEnabled() {
        if (view == null)
            view = list.getView();
        enabled = view.hasSelectedDocument();
        if (enabled)
            initVars();
        return enabled;
    }
}

