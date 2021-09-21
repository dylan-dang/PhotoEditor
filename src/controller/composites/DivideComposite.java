package controller.composites;

public class DivideComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src == 0)
            return 1;
        return Math.min(1, dst / src);
    }

    @Override
    public String toString() {
        return "Divide";
    }
}
