package controller.composites;

public class ExclusionComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return dst + src - 2 * dst * src;
    }

    @Override
    public String toString() {
        return "Exclusion";
    }
}
