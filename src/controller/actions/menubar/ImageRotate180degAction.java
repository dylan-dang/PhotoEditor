package controller.actions.menubar;

import java.util.*;
import java.awt.event.*;

import view.View;
import model.Layer;

public class ImageRotate180degAction extends MenuBarAction {
    public ImageRotate180degAction(View view) {
        super(view, "Rotate 180°", "ctrl J");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

        createSnapshot();
        for (Layer layer : layers) {
            layer.rotate180deg();
        }

        updateDocView();
    }
}

