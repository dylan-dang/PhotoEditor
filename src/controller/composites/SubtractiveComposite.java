package controller.composites;

public class SubtractiveComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return src + dst - 1;
    }

    @Override
    public String toString() {
        return "Subtractive (Linear Burn)";
    }
}
