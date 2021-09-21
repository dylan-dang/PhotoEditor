package controller.composites;

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
