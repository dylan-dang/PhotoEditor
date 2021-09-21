package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class TogglePixelGrid extends MenuBarAction {
    public TogglePixelGrid(View view) {
        super(view, "Turn Pixel Grid On");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean pixelGridEnabled = !view.isPixelGridEnabled();
        view.setPixelGridEnabled(pixelGridEnabled);
        if (pixelGridEnabled) {
            putValue(NAME, "Turn Pixel Grid Off");
        } else {
            putValue(NAME, "Turn Pixel Grid On");
        }
    }
}

// Image Actions

