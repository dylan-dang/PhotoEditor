package model;

import java.util.*;

public class SnapShot {
    private ArrayList<Layer> layers = new ArrayList<Layer>();
    private int height, width;
    private int selectedLayer;

    SnapShot(Document doc, int selectedLayer) {
        height = doc.getHeight();
        width = doc.getWidth();
        for (Layer layer : doc.getLayers()) {
            layers.add(layer.copy());
        }
        this.selectedLayer = selectedLayer;
    }

    public ArrayList<Layer> getLayers() {
        return layers;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getSelectedLayer() {
        return selectedLayer;
    }
}
