package controller.composites;

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
