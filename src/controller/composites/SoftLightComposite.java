package controller.composites;

public class SoftLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0.5)
            return dst - (1 - 2 * src) * dst * (1 - dst);
        return dst + (2 * src - 1) * (d(dst) - dst);
    }

    private float d(float x) {
        if (x <= .25)
            return ((16 * x - 12) * x + 4) * x;
        return (float) Math.sqrt(x);
    }

    @Override
    public String toString() {
        return "Soft Light";
    }
}
