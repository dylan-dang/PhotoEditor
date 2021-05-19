
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

public class FantasyFilter extends FilterAction {
  FantasyFilter(View view) {
    super(view, "Fantasy Filter");
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    for(int i = 0; i < pixels.length; i++) {
      pixels[i] = getColor(alpha(pixels[i]), blue(pixels[i]) + 83, green(pixels[i]) + 80, red(pixels[i]) + 80);
    }
    update();
  }
}

public class BlackAndWhiteFilter extends FilterAction {
  BlackAndWhiteFilter(View view) {
    super(view, "Black and White");
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    for(int i = 0; i < pixels.length; i++) {
      int average = red(pixels[i])/3 + green(pixels[i])/3 + blue(pixels[i])/3;
      pixels[i] = getColor(alpha(pixels[i]), average, average, average);
    }
    update();
  }
}

public class SepiaFilter extends FilterAction {
  SepiaFilter(View view) {
    super(view, "Sepia");
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    for(int i = 0; i < pixels.length; i++) {
      int r = red(pixels[i]);
      int g = green(pixels[i]);
      int b = blue(pixels[i]);
      pixels[i] = getColor(alpha(pixels[i]),
        (int)(0.393*r + 0.769*g + 0.189*b),
        (int)(0.349*r + 0.686*g + 0.168*b),
        (int)(0.272*r + 0.534*g + 0.131*b));
    }
    update();
  }
}

public class InvertFilter extends FilterAction {
  InvertFilter(View view) {
    super(view, "Invert");
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    for(int i = 0; i < pixels.length; i++) {
      pixels[i] = getColor(alpha(pixels[i]),
      255 - red(pixels[i]),
      255 - green(pixels[i]),
      255 - blue(pixels[i]));
    }
    update();
  }
}

public class PosterizeFilter extends FilterAction {
  PosterizeFilter(View view) {
    super(view, "Posterize");
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    for(int i = 0; i < pixels.length; i++) {
      int r = red(pixels[i]);
      int g = green(pixels[i]);
      int b = blue(pixels[i]);
      pixels[i] = getColor(alpha(pixels[i]), 
        r - r % 64,
        g - g % 64,
        b - b % 64
      );
    }
    update();
  }
}

public class ChromaticAbberationFilter extends FilterAction {
  ChromaticAbberationFilter(View view) {
    super(view, "Chromatic Abberation");
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    int[] original = pixels.clone();
    for(int i = 0; i < pixels.length; i++) {
      pixels[i] = getColor(alpha(pixels[i]), 
        red(original[Math.floorMod(i - 5, pixels.length)]),
        green(original[i]),
        blue(original[(i + 5) % pixels.length]));
    }
    update();
  }
}