package controller.filters;

import java.awt.event.*;
import java.awt.image.*;
import view.View;
import controller.actions.menubar.MenuBarAction;

public abstract class FilterAction extends MenuBarAction {
    protected BufferedImage image;
    protected int[] pixels;

    FilterAction(View view, String name) {
        super(view, name);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        image = view.getSelectedDocumentView().getSelectedLayer().getImage();
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        view.getSelectedDocumentView().save();
    }

    protected int alpha(int argb) {
        return (argb >> 24) & 0xFF;
    }

    protected int red(int argb) {
        return (argb >> 16) & 0xFF;
    }

    protected int green(int argb) {
        return (argb >> 8) & 0xFF;
    }

    protected int blue(int argb) {
        return argb & 0xFF;
    }

    protected int getColor(int a, int r, int g, int b) {
        r = Math.min(Math.max(r, 0), 255);
        g = Math.min(Math.max(g, 0), 255);
        b = Math.min(Math.max(b, 0), 255);
        return a << 24 | r << 16 | g << 8 | b;
    }

    protected void update() {
        view.getSelectedDocument().updateFlattenedCache();
        view.getSelectedDocumentView().getCanvas().repaint();
    }
}

