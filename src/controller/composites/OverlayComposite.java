package controller.composites;

public class OverlayComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (dst <= 0.5)
            return src * dst * 2; // multiply
        return 1 - 2 * (1 - src) * (1 - dst); // screen
    }

    @Override
    public String toString() {
        return "Overlay";
    }
}
