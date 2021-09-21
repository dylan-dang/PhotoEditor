package controller.composites;

public class ColorDodgeComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src >= 1)
            return 1;
        return Math.min(1, dst / (1 - src));
    }

    @Override
    public String toString() {
        return "Color Dodge";
    }
}
