package controller.composites;

public class AndComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return dst & 0xFFFFFF & src;
    }

    @Override
    public String toString() {
        return "And";
    }
}
