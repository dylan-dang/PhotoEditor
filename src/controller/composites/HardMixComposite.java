package controller.composites;

public class HardMixComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src < 1 - dst)
            return 0;
        return 1;
    }

    @Override
    public String toString() {
        return "Hard Mix";
    }
}
