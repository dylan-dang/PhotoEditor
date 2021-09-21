package controller.filters;

import java.util.Arrays;
import java.util.Random;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import view.View;

public class WarholBiggieFilter extends FilterAction {
    BufferedImage buffer;
    int[] bufferData;
    Graphics2D g;
    Random random;

    public WarholBiggieFilter(View view) {
        super(view, "Warhol Biggie Filter");
        random = new Random();
    }

    private void drawCell(int[] data, int row, int col) {
        bufferData = Arrays.copyOf(data, data.length);
        int width = image.getWidth() / 3;
        int height = image.getHeight() / 3;
        g.drawImage(buffer, col * width, row * height, width, height, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);

        buffer = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        bufferData = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
        g = image.createGraphics();

        int[] original = pixels.clone();
        g.clearRect(0, 0, image.getWidth(), image.getHeight());

        drawCell(ampedRed(original), 0, 0);
        drawCell(ampedGreen(original), 0, 1);
        drawCell(ampedBlue(original), 0, 2);

        drawCell(redGreen(original), 1, 0);
        drawCell(original, 1, 1);
        drawCell(greenBlue(original), 1, 2);

        drawCell(dimmed(original), 2, 0);
        drawCell(blueGreen(original), 2, 1);
        drawCell(redBlue(original), 2, 2);

        g.dispose();
        update();
    }

    private int[] ampedRed(int[] pixels) {
        int[] ampedRed = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            ampedRed[i] = getColor(alpha(pixels[i]), red(pixels[i] * (pixels.length / 3)),
                    green(pixels[i]), blue(pixels[i]));
        }
        return ampedRed;
    }

    private int[] ampedGreen(int[] pixels) {
        int[] ampedGreen = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            ampedGreen[i] = getColor(alpha(pixels[i]), red(pixels[i]),
                    green(pixels[i] * (pixels.length / 3)), blue(pixels[i]));
        }
        return ampedGreen;
    }

    private int[] ampedBlue(int[] pixels) {
        int[] ampedBlue = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            ampedBlue[i] = getColor(alpha(pixels[i]), red(pixels[i]), green(pixels[i]),
                    blue(pixels[i]) * (pixels.length / 3));
        }
        return ampedBlue;
    }

    private int[] redGreen(int[] pixels) {
        int[] redGreen = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            redGreen[i] =
                    getColor(alpha(pixels[i]), red(pixels[i]) * (pixels.length * green(pixels[i])),
                            green(pixels[i]) * (pixels.length * red(pixels[i])), blue(pixels[i]));
        }
        return redGreen;
    }

    private int[] greenBlue(int[] pixels) {
        int[] greenBlue = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            greenBlue[i] = getColor(alpha(pixels[i]), red(pixels[i]),
                    green(pixels[i]) * (pixels.length * blue(pixels[i])),
                    blue(pixels[i]) * (pixels.length * green(pixels[i])));
        }
        return greenBlue;
    }

    private int[] dimmed(int[] pixels) {
        int[] dimmed = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            dimmed[i] = getColor(alpha(pixels[i]), red(pixels[i]) * randIntAboveZero(10),
                    green(pixels[i]) * randIntAboveZero(15),
                    blue(pixels[i]) * randIntAboveZero(20));
        }
        return dimmed;
    }

    private int[] blueGreen(int[] pixels) {
        int[] blueGreen = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            blueGreen[i] = getColor(alpha(pixels[i]), red(pixels[i]) / randIntAboveZero(5),
                    green(pixels[i]) / randIntAboveZero(5), blue(pixels[i]) / randIntAboveZero(5));

        }
        return blueGreen;
    }

    private int[] redBlue(int[] pixels) {
        int[] redBlue = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            redBlue[i] = getColor(alpha(pixels[i]), red(pixels[i]) % randIntAboveZero(256),
                    green(pixels[i]) % randIntAboveZero(256),
                    blue(pixels[i]) % randIntAboveZero(256));
        }
        return redBlue;
    }

    private int randIntAboveZero(int n) {
        return random.nextInt(n) + 1;
    }
}

