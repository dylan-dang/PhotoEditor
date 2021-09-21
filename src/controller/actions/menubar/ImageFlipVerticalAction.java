package controller.actions.menubar;

import java.util.*;
import java.awt.event.*;
import view.View;
import model.Layer;

public class ImageFlipVerticalAction extends MenuBarAction {
    public ImageFlipVerticalAction(View view) {
        super(view, "Flip Vertical");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ArrayList<Layer> layers = view.getSelectedDocument().getLayers();

        createSnapshot();
        for (Layer layer : layers) {
            layer.flipVertically();
        }

        updateDocView();
    }
}

