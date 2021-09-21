package controller.composites;

public class DarkenComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return Math.min(src, dst);
    }

    @Override
    public String toString() {
        return "Darken";
    }
}
