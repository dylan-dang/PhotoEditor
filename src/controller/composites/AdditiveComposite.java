package controller.composites;

public class AdditiveComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return src + dst;
    }

    @Override
    public String toString() {
        return "Additive (Linear Dodge)";
    }
}
