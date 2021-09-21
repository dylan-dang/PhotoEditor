package model;

import java.awt.*;
import java.awt.image.*;

import controller.composites.*;
import static utils.Constants.BLEND_MODES;

public class Layer {
    BufferedImage image;
    private String name = "Background";
    private boolean visibility = true;
    private float opacity = 1f;
    private Graphics2D g;
    private int blendIndex = 0;

    public Layer(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalStateException("Expected TYPE_INT_ARGB");
        }
        this.image = image;
        g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalStateException("Expected TYPE_INT_ARGB");
        }
        g.dispose();
        this.image = image;
        g = image.createGraphics();
    }

    public void crop(Rectangle rect) {
        g.dispose();
        BufferedImage crop = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
        image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        g.drawImage(crop, 0, 0, null);
    }

    public void flipHorizontally() {
        Composite before = g.getComposite();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(image, image.getWidth(), 0, -image.getWidth(), image.getHeight(), null);
        g.setComposite(before);
    }

    public void flipVertically() {
        Composite before = g.getComposite();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(getCopiedImage(), 0, image.getHeight(), image.getWidth(), -image.getHeight(),
                null);
        g.setComposite(before);
    }

    public void rotate180deg() {
        Composite before = g.getComposite();
        g.setComposite(AlphaComposite.Src);
        g.rotate(Math.PI, image.getWidth() / 2, image.getHeight() / 2);
        g.drawRenderedImage(getCopiedImage(), null);
        g.rotate(Math.PI, image.getWidth() / 2, image.getHeight() / 2);
        g.setComposite(before);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BlendComposite getBlendComposite() {
        return BLEND_MODES[blendIndex];
    }

    public int getBlendIndex() {
        return blendIndex;
    }

    public void setBlendComposite(int index) {
        blendIndex = index;
    }

    public boolean isVisible() {
        return visibility;
    }

    public void setVisible(boolean visibility) {
        this.visibility = visibility;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = Math.max(0, Math.min(1, opacity));
    }

    public Graphics2D getGraphics() {
        return g;
    }

    public Layer copy() {
        Layer copy = new Layer(getCopiedImage());
        copy.setName(name);
        copy.setOpacity(opacity);
        copy.setVisible(isVisible());
        copy.setBlendComposite(blendIndex);
        return copy;
    }

    public BufferedImage getCopiedImage() {
        ColorModel colorModel = image.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
        BufferedImage imageCopy = new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
        return imageCopy;
    }
}
