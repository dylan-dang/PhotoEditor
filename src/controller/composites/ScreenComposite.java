package controller.composites;

public class ScreenComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return dst - src - dst * src;
    }

    @Override
    public String toString() {
        return "Screen";
    }
}
