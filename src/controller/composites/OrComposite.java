package controller.composites;

public class OrComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return src | dst & 0xFFFFFF;
    }

    @Override
    public String toString() {
        return "Or";
    }
}
