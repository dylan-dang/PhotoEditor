public abstract class BlendComposite implements Composite, CompositeContext {
  protected int width, height;
  protected int[] srcPixels, dstPixels;
  protected float opacity = 1;

  public void setOpacity(float opacity) {
    this.opacity = Math.max(0, Math.min(1, opacity));
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
    return ((int)(255*a) << 24) |  ((int)(255*r) << 16) | ((int)(255*g) << 8) | (int)(255*b);
  }

  //floats simplify division and multiplication operations
  protected float alpha(int c) {
    return (float)((c >> 24) & 0xFF) / 255f;
  }

  protected float red(int c) {
    return (float)((c >> 16) & 0xFF) / 255f;
  }

  protected float green(int c) {
    return (float)((c >> 8) & 0xFF) / 255f;
  }

  protected float blue(int c) {
    return (float)(c & 0xFF) / 255f;
  }

  protected int overPixel(int src, int dst) {
    float aSrc = alpha(src);
    float aDst = alpha(dst);
    aSrc *= opacity;
    aDst = aDst * (1 - aSrc);

    float a = aSrc + aDst;
    if (a == 0) return 0;
    float r = (red(src)*aSrc + red(dst)*aDst) / a;
    float g = (green(src)*aSrc + green(dst)*aDst) / a;
    float b = (blue(src)*aSrc + blue(dst)*aDst) / a;
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

public abstract class SeparableBlendComponent extends BlendComposite {
  @Override
  protected int blendPixel(int src, int dst) {
    float r = blendChannel(red(src), red(dst));
    float g = blendChannel(green(src), green(dst));
    float b = blendChannel(blue(src), blue(dst));
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
}

public class MultiplyComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    return src * dst;
  }
}

public class ScreenComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    return dst - src - dst*src;
  }
}

public class Overlay extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    if (dst <= 0.5)
      return src * dst * 2; //multiply
    dst = 2 * dst - 1;
    return src - dst - dst*src; //screen
  }
}

public class DarkenComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    return Math.min(src, dst);
  }
}

public class LightenComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    return Math.max(src, dst);
  }
}

public class AdditiveComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    return Math.min(src + dst, 1);
  }
}

public class ColorDodgeComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    if (src >= 1) return 1;
    return Math.min(1, dst / (1 - src));
  }
}

public class ColorBurnComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    if (src <= 0) return 0;
    return 1 - Math.min(1, (1 - dst) / src);
  }
}

public class HardLightComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    if (src <= 0.5)
      return dst * src * 2; //multiply
    src = 2 * src - 1;
    return dst - src - dst*src; //screen
  }
}

public class SoftLightComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    if (src <= 0.5)
      return dst - (1-2*src) * dst * (1-dst);
    return dst + (2*src-1) * (d(dst) - dst);
  }
  private float d(float x) {
    if (x <= .25)
      return ((16*x - 12) * x + 4) * x;
    return (float)Math.sqrt(x);
  }
}

public class DifferenceComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    return Math.abs(dst - src);
  }
}

public class ExclusionComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    return dst + src - 2*dst*src;
  }
}

public class DivideComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    if (src == 0) return 1;
    return Math.min(1, dst / src);
  }
}

public class AverageComposite extends SeparableBlendComponent {
  @Override
  protected float blendChannel(float src, float dst) {
    return (src + dst) / 2;
  }
}

public class XORComposite extends BlendComposite {
  @Override
  protected int blendPixel(int src, int dst) {
    return src ^ dst & 0xFFFFFF;
   }
}
