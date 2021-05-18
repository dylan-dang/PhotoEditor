
public abstract class FilterAction extends MenuBarAction {
  protected BufferedImage image;
  protected int[] pixels;

  FilterAction(View view, String name) {
    super(view, name);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    image = view.getSelectedDocumentView().getSelectedLayer().getImage();
    pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
    view.getSelectedDocumentView().save();
  }
  
  protected int alpha(int argb) {
    return (argb >> 24) & 0xFF;
  }

  protected int red(int argb) {
    return (argb >> 16) & 0xFF;
  }

  protected int green(int argb) {
    return (argb >> 8) & 0xFF;
  }

  protected int blue(int argb) {
    return argb & 0xFF;
  }

  protected int getColor(int a, int r, int g, int b) {
    r = constrain(r, 0, 255);
    g = constrain(g, 0, 255);
    b = constrain(b, 0, 255);
    return a<<24 | r<<16 | g<<8 | b;
  }
  
  protected void update() {
    view.getSelectedDocument().updateFlattenedCache();
    view.getSelectedDocumentView().getCanvas().repaint();
  }
}

public class PopArtFilter extends FilterAction {
  PopArtFilter(View view) {
    super(view, "Pop Art Filter");
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    int tempPixel1 = 0;
    for(int i = 0; i < pixels.length; i++) {
      if (i % 3 == 0) {
        int tempPixel2 = pixels[i];
        pixels[i] = getColor(alpha(tempPixel1), red(tempPixel1), green(tempPixel1), blue(tempPixel1));
        tempPixel1 = tempPixel2;
      }
      i += 5;
    }
    update();
  }
}

public class WarholBiggieFilter extends FilterAction {
  BufferedImage buffer;
  int[] bufferData;
  Graphics2D g;

  WarholBiggieFilter(View view) {
    super(view, "Warhol Biggie Filter");
  }

  void drawCell(int[] data, int row, int col) {
    arrayCopy(data, bufferData);
    int width = image.getWidth() / 3;
    int height = image.getHeight() / 3;
    g.drawImage(buffer, col * width, row * height, width, height, null);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);

    buffer = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
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

  int[] ampedRed(int[] pixels) {
    int[] ampedRed = new int[pixels.length];
    for(int i = 0; i < pixels.length; i++) {
      ampedRed[i] = getColor(
        alpha(pixels[i]),
        red(pixels[i] * (pixels.length / 3)),
        green(pixels[i]),
        blue(pixels[i]));
    }
    return ampedRed;
  }

  int[] ampedGreen(int[] pixels) {
    int[] ampedGreen = new int[pixels.length];
    for(int i = 0; i < pixels.length; i++) {
      ampedGreen[i] = getColor(
        alpha(pixels[i]),
        red(pixels[i]),
        green(pixels[i] * (pixels.length / 3)),
        blue(pixels[i]));
    }
    return ampedGreen;
  }

  int[] ampedBlue(int[] pixels) {
    int[] ampedBlue = new int[pixels.length];
    for(int i = 0; i < pixels.length; i++) {
      ampedBlue[i] = getColor(
        alpha(pixels[i]),
        red(pixels[i]),
        green(pixels[i]),
        blue(pixels[i]) * (pixels.length / 3));
    }
    return ampedBlue;
  }

  int[] redGreen(int[] pixels) {
    int[] redGreen = new int[pixels.length];
    for(int i = 0; i < pixels.length; i++) {
      redGreen[i] = getColor(
        alpha(pixels[i]),
        red(pixels[i]) * (pixels.length * green(pixels[i])),
        green(pixels[i]) * (pixels.length * red(pixels[i])),
        blue(pixels[i]));
    }
    return redGreen;
  }
  
  int[] greenBlue(int[] pixels) {
    int[] greenBlue = new int[pixels.length];
    for(int i = 0; i < pixels.length; i++) {
      greenBlue[i] = getColor(
        alpha(pixels[i]),
        red(pixels[i]),
        green(pixels[i]) * (pixels.length * blue(pixels[i])),
        blue(pixels[i]) * (pixels.length * green(pixels[i])));
    }
    return greenBlue;
  }

  int[] dimmed(int[] pixels) {
    int[] dimmed = new int[pixels.length];
    for(int i = 0; i < pixels.length; i++) {
      dimmed[i] = getColor(
        alpha(pixels[i]),
        red(pixels[i]) * (int) random(1, 10),
        green(pixels[i]) * (int) random(1, 15),
        blue(pixels[i]) * (int) random(1, 20));
    }
    return dimmed;
  }

  int[] blueGreen(int[] pixels) {
    int[] blueGreen = new int[pixels.length];
    for(int i = 0; i < pixels.length; i++) {
      blueGreen[i] = getColor(
        alpha(pixels[i]),
        red(pixels[i]) / (int) random(1, 5),
        green(pixels[i]) / (int) random(1, 5),
        blue(pixels[i]) / (int) random(1, 5));

    }
    return blueGreen;
  }

  int[] redBlue(int[] pixels) {
    int[] redBlue = new int[pixels.length];
    for(int i = 0; i < pixels.length; i++) {
      redBlue[i] = getColor(
        alpha(pixels[i]),
        red(pixels[i]) % (int) random(1, 256),
        green(pixels[i]) % (int) random(1, 256),
        blue(pixels[i]) % (int) random(1, 256));
    }
    return redBlue;
  }
}

public class TrippyFilter extends FilterAction {
  TrippyFilter(View view) {
    super(view, "Trippy Filter");
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    for(int i = 0; i < pixels.length; i++) {
      pixels[i] = tintColor(pixels[i], 175);
      pixels[i] = color(alpha(pixels[i]), blue(pixels[i]) + 3, green(pixels[i]), red(pixels[i]));
    }
    update();
  }
  private int tintChannel(int channel, int tint) {
    return channel + (255 - channel) * tint;
  }
  private int tintColor(int c, int tint) {
    return getColor(alpha(c), tintChannel(red(c), tint), tintChannel(green(c), tint), tintChannel(blue(c), tint));
  }
}
