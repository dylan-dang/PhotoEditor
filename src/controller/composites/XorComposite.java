package controller.composites;

public class XorComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return src ^ dst & 0xFFFFFF;
    }

    @Override
    public String toString() {
        return "Xor";
    }
}
