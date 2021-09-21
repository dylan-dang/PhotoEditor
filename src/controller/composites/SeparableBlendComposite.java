package controller.composites;

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
