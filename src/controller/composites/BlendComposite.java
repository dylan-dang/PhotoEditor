package controller.composites;

import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.ColorModel;
import java.awt.RenderingHints;
import java.awt.Composite;
import java.awt.CompositeContext;

public abstract class BlendComposite implements Composite, CompositeContext {
    protected int width, height;
    protected int[] srcPixels, dstPixels;
    protected float opacity = 1;

    protected float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    public void setOpacity(float opacity) {
        this.opacity = clamp(opacity);
    }

    public float getOpacity() {
        return opacity;
    }

    protected void checkType(Raster raster) {
        if (raster.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
            throw new IllegalStateException("Expected integer sample type");
        }
    }

    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        checkType(src);
        checkType(dstIn);
        checkType(dstOut);

        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());
        int[] srcPixels = ((DataBufferInt) src.getDataBuffer()).getData().clone();
        int[] dstPixels = ((DataBufferInt) dstIn.getDataBuffer()).getData();

        for (int i = 0; i < dstPixels.length; i++) {
            srcPixels[i] = blendPixel(srcPixels[i], dstPixels[i]);
            srcPixels[i] = overPixel(srcPixels[i], dstPixels[i]);
        }

        dstOut.setDataElements(0, 0, width, height, srcPixels);
    }

    protected int toColor(float r, float g, float b, float a) {
        return ((int) (255 * a) << 24) | ((int) (255 * r) << 16) | ((int) (255 * g) << 8)
                | (int) (255 * b);
    }

    protected float alpha(int c) {
        return (float) ((c >> 24) & 0xFF) / 255f;
    }

    protected float red(int c) {
        return (float) ((c >> 16) & 0xFF) / 255f;
    }

    protected float green(int c) {
        return (float) ((c >> 8) & 0xFF) / 255f;
    }

    protected float blue(int c) {
        return (float) (c & 0xFF) / 255f;
    }

    protected int overPixel(int src, int dst) {
        float aSrc = alpha(src);
        float aDst = alpha(dst);
        aSrc *= opacity;
        aDst = aDst * (1 - aSrc);

        float a = aSrc + aDst;
        if (a == 0)
            return 0;
        float r = (red(src) * aSrc + red(dst) * aDst) / a;
        float g = (green(src) * aSrc + green(dst) * aDst) / a;
        float b = (blue(src) * aSrc + blue(dst) * aDst) / a;
        return toColor(r, g, b, a);
    }

    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel,
            RenderingHints hints) {
        return this;
    }

    @Override
    public void dispose() {}

    protected abstract int blendPixel(int src, int dst);
}
