package controller.composites;

public class ColorBurnComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0)
            return 0;
        return 1 - Math.min(1, (1 - dst) / src);
    }

    @Override
    public String toString() {
        return "Color Burn";
    }
}
