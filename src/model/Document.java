package model;

import java.util.*;

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;
import javax.imageio.*;

import java.io.File;
import java.io.IOException;
import controller.composites.*;

public class Document {
    ArrayList<Layer> layers = new ArrayList<Layer>();
    private int height, width;
    private boolean isSaved = true;
    private BufferedImage flattened;
    private File linkedFile;

    public Document(int width, int height) {
        this.width = width;
        this.height = height;
        Layer layer = new Layer(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        layers.add(layer);
    }

    public Document(File file) {
        linkedFile = file;
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Something went wrong when trying to read your file.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        width = image.getWidth();
        height = image.getHeight();
        layers.add(new Layer(toRGBA(image)));
    }

    public Document(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        layers.add(new Layer(toRGBA(image)));
    }

    private BufferedImage toRGBA(BufferedImage image) {
        BufferedImage rbgaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rbgaImage.createGraphics();
        g2.drawImage(image, null, 0, 0);
        g2.dispose();
        return rbgaImage;
    }

    public String getName() {
        if (linkedFile != null) {
            return linkedFile.getName();
        }
        return "Untitled";
    }

    public BufferedImage flattened() {
        if (flattened == null)
            updateFlattenedCache();
        return flattened;
    }

    // a real bottleneck for performance but it'll have to do for now, perhaps cache the
    // preflattened layer below
    public void updateFlattenedCache() {
        flattened = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = flattened.createGraphics();
        for (Layer layer : layers) {
            if (!layer.isVisible())
                continue;
            BlendComposite blendComposite = layer.getBlendComposite();
            blendComposite.setOpacity(layer.getOpacity());
            g2.setComposite(blendComposite);
            g2.drawImage(layer.getImage(), null, 0, 0);
        }
        g2.dispose();
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean value) {
        isSaved = value;
    }

    public boolean isLinked() {
        return linkedFile != null;
    }

    public File getLinkedFile() {
        return linkedFile;
    }

    public void setLinkedFile(File file) {
        this.linkedFile = file;
    }

    public ArrayList<Layer> getLayers() {
        return layers;
    }

    public int getLayerCount() {
        return layers.size();
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Dimension getDimension() {
        return new Dimension(width, height);
    }

    public Layer addEmptyLayer(int index) {
        Layer layer = new Layer(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        layer.setName(String.format("Layer %d", layers.size()));
        layers.add(index, layer);
        return layer;
    }

    public Layer addEmptyLayer() {
        return addEmptyLayer(layers.size());
    }

    public void addLayer(Layer layer, int index) {
        layers.add(index, layer);
    }

    public void addLayer(Layer layer) {
        layers.add(layer);
    }

    public void crop(Rectangle rect) {
        for (Layer layer : layers) {
            layer.crop(rect);
        }
        width = rect.width;
        height = rect.height;
    }

    public void resize(int width, int height, Object interpolation) {
        this.width = width;
        this.height = height;
        for (Layer layer : layers) {
            BufferedImage original = layer.getImage();
            layer.setImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));

            Graphics2D g = layer.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
            g.drawImage(original, 0, 0, width, height, null);
        }
    }

    public void changeCanvasSize(int width, int height, String anchor) {
        int x = 0, y = 0;
        if (anchor.startsWith("Top"))
            y = 0;
        else if (anchor.startsWith("Center"))
            y = (height - this.height) / 2;
        else if (anchor.startsWith("Bottom"))
            y = height - this.height;

        if (anchor.endsWith("Left"))
            x = 0;
        else if (anchor.endsWith("Center"))
            x = (width - this.width) / 2;
        else if (anchor.endsWith("Right"))
            x = width - this.width;

        this.width = width;
        this.height = height;

        for (Layer layer : layers) {
            BufferedImage original = layer.getImage();
            layer.setImage(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));

            Graphics2D g = layer.getGraphics();

            g.drawImage(original, x, y, null);
        }
    }
}
