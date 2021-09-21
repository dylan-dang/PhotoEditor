package controller.actions.menubar;

import java.util.*;
import java.awt.event.*;
import view.View;
import model.Layer;

public class ImageFlipHorizontalAction extends MenuBarAction {
    public ImageFlipHorizontalAction(View view) {
        super(view, "Flip Horizontal");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

        createSnapshot();
        for (Layer layer : layers) {
            layer.flipHorizontally();
        }

        updateDocView();
    }
}

