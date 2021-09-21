package controller.composites;

public class NormalComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return src;
    }

    @Override
    public String toString() {
        return "Normal";
    }
}
