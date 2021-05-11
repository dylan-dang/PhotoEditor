public final BlendComposite[] BLEND_MODES = new BlendComposite[] {
    new NormalComposite(),
        null,
        new DarkenComposite(),
        new MultiplyComposite(),
        new ColorBurnComposite(),
        new SubtractiveComposite(),
        null,
        new LightenComposite(),
        new ScreenComposite(),
        new ColorDodgeComposite(),
        new AdditiveComposite(),
        null,
        new OverlayComposite(),
        new SoftLightComposite(),
        new HardLightComposite(),
        new VividLightComposite(),
        new LinearLightComposite(),
        new PinLightComposite(),
        new HardMixComposite(),
        null,
        new DifferenceComposite(),
        new ExclusionComposite(),
        null,
        new XorComposite(),
        new AndComposite(),
        new OrComposite()
    };


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
        int[] srcPixels = ((DataBufferInt)src.getDataBuffer()).getData().clone();
        int[] dstPixels = ((DataBufferInt)dstIn.getDataBuffer()).getData();
        
        for (int i = 0; i < dstPixels.length; i++) {
            srcPixels[i] = blendPixel(srcPixels[i], dstPixels[i]);
            srcPixels[i] = overPixel(srcPixels[i], dstPixels[i]);
        }
        
        dstOut.setDataElements(0, 0, width, height, srcPixels);
    }
    
    protected int toColor(float r, float g, float b, float a) {
        return((int)(255 * a) << 24) |   ((int)(255 * r) << 16) | ((int)(255 * g) << 8) | (int)(255 * b);
    }
    
    protected float alpha(int c) {
        //floats simplify division and multiplication operations
        return(float)((c >> 24) & 0xFF) / 255f;
    }
    
    protected float red(int c) {
        return(float)((c >> 16) & 0xFF) / 255f;
    }
    
    protected float green(int c) {
        return(float)((c >> 8) & 0xFF) / 255f;
    }
    
    protected float blue(int c) {
        return(float)(c & 0xFF) / 255f;
    }
    
    protected int overPixel(int src, int dst) {
        float aSrc = alpha(src);
        float aDst = alpha(dst);
        aSrc *= opacity;
        aDst = aDst * (1 - aSrc);
        
        float a = aSrc + aDst;
        if (a == 0) return 0;
        float r = (red(src) * aSrc + red(dst) * aDst) / a;
        float g = (green(src) * aSrc + green(dst) * aDst) / a;
        float b = (blue(src) * aSrc + blue(dst) * aDst) / a;
        return toColor(r, g, b, a);
    }
    
    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return this;
    }
    
    @Override
    public void dispose() {}
    
    protected abstract int blendPixel(int src, int dst);
}

public abstract class SeparableBlendComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        float r = clamp(blendChannel(red(src), red(dst)));
        float g = clamp(blendChannel(green(src), green(dst)));
        float b = clamp(blendChannel(blue(src), blue(dst)));
        float a = alpha(src);
        
        return toColor(r, g, b, a);
    }
    
    protected abstract float blendChannel(float src, float dst);
}

public class NormalComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return src;
    }
    
    @Override
    public String toString() {
        return "Normal";
    }
}

public class MultiplyComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return src * dst;
    }
    
    @Override
    public String toString() {
        return "Multiply";
    }
}

public class ScreenComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return dst - src - dst * src;
    }
    
    @Override
    public String toString() {
        return "Screen";
    }
}

public class OverlayComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (dst <= 0.5)
            return src * dst * 2; //multiply
        return 1 - 2 * (1 - src) * (1 - dst); //screen
    }
    
    @Override
    public String toString() {
        return "Overlay";
    }
}

public class DarkenComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return Math.min(src, dst);
    }
    
    @Override
    public String toString() {
        return "Darken";
    }
}

public class LightenComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return Math.max(src, dst);
    }
    
    @Override
    public String toString() {
        return "Lighten";
    }
}

public class AdditiveComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return src + dst;
    }
    
    @Override
    public String toString() {
        return "Additive (Linear Dodge)";
    }
}
public class SubtractiveComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return src + dst - 1;
    }
    
    @Override
    public String toString() {
        return "Subtractive (Linear Burn)";
    }
}

public class ColorDodgeComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src >= 1) return 1;
        return Math.min(1, dst / (1 - src));
    }
    
    @Override
    public String toString() {
        return "Color Dodge";
    }
}

public class ColorBurnComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0) return 0;
        return 1 - Math.min(1,(1 - dst) / src);
    }
    
    @Override
    public String toString() {
        return "Color Burn";
    }
}

public class HardLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0.5)
            return dst * src * 2; //multiply
        return 1 - 2 * (1 - src) * (1 - dst); //screen
    }
    
    @Override
    public String toString() {
        return "Hard Light";
    }
}

public class SoftLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0.5)
            return dst - (1 - 2 * src) * dst * (1 - dst);
        return dst + (2 * src - 1) * (d(dst) - dst);
    }
    
    private float d(float x) {
        if (x <=.25)
            return((16 * x - 12) * x + 4) * x;
        return(float)Math.sqrt(x);
    }
    
    @Override
    public String toString() {
        return "Soft Light";
    }
}

public class VividLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0.5)
            return 1 - (1 - dst) / (2 * src);
        return dst / (2 * (1 - src));
    }
    
    @Override
    public String toString() {
        return "Vivid Light";
    }
}

public class LinearLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return dst + 2 * src - 1;
    }
    
    @Override
    public String toString() {
        return "Linear Light";
    }
}

public class PinLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (dst < 2 * src - 1)
            return 2 * src - 1;
        if (dst < 2 * src)
            return dst;
        return 2 * src;
    }
    
    @Override
    public String toString() {
        return "Pin Light";
    }
}

public class HardMixComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src < 1 - dst) return 0;
        return 1;
    }
    
    @Override
    public String toString() {
        return "Hard Mix";
    }
}

public class DifferenceComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return Math.abs(dst - src);
    }
    
    @Override
    public String toString() {
        return "Difference";
    }
}

public class ExclusionComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return dst + src - 2 * dst * src;
    }
    
    @Override
    public String toString() {
        return "Exclusion";
    }
}

public class DivideComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src == 0) return 1;
        return Math.min(1, dst / src);
    }
    
    @Override
    public String toString() {
        return "Divide";
    }
}

public class XorComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return src ^ dst & 0xFFFFFF;
    }
    
    @Override
    public String toString() {
        return "Xor";
    }
}

public class AndComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return dst & 0xFFFFFF & src;
    }
    
    @Override
    public String toString() {
        return "And";
    }
}
public class OrComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return src | dst & 0xFFFFFF;
    }
    
    @Override
    public String toString() {
        return "Or";
    }
}
