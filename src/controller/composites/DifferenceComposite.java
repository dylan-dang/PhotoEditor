package controller.composites;

public class DifferenceComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return Math.abs(dst - src);
    }

    @Override
    public String toString() {
        return "Difference";
    }
}
