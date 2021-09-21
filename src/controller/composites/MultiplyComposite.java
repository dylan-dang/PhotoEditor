package controller.composites;

public class MultiplyComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return src * dst;
    }

    @Override
    public String toString() {
        return "Multiply";
    }
}
