package controller.composites;

public class HardLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0.5)
            return dst * src * 2; // multiply
        return 1 - 2 * (1 - src) * (1 - dst); // screen
    }

    @Override
    public String toString() {
        return "Hard Light";
    }
}
