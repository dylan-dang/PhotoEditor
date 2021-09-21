package controller.composites;

public class LightenComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return Math.max(src, dst);
    }

    @Override
    public String toString() {
        return "Lighten";
    }
}
