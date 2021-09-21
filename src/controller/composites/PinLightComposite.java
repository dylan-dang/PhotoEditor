package controller.composites;

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
