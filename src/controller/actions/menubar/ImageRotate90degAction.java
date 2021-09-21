package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import view.View;
import model.Document;
import model.Layer;

public class ImageRotate90degAction extends MenuBarAction {
    private boolean clockwise;

    public ImageRotate90degAction(View view, boolean clockwise) {
        super(view, String.format("Rotate 90° %s", clockwise ? "Clockwise" : "Counter-Clockwise"),
                String.format("ctrl %s", clockwise ? "H" : "G"));
        this.clockwise = clockwise;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Document document = view.getSelectedDocument();
        int width = document.getWidth();
        int height = document.getHeight();

        createSnapshot();

        for (Layer layer : document.getLayers()) {
            BufferedImage rotated = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = rotated.createGraphics();
            int center = clockwise ? (height - width) / 2 : (width - height) / 2;
            g.translate(center, center);
            g.rotate((clockwise ? 1 : 3) * Math.PI / 2, height / 2, width / 2);
            g.drawRenderedImage(layer.getImage(), null);
            g.dispose();
            layer.setImage(rotated);
        }

        document.setHeight(width);
        document.setWidth(height);
        updateDocView();
    }

}

