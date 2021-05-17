
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
  WarholBiggieFilter(View view) {
    super(view, "Warhol Biggie Filter");
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    super.actionPerformed(e);
    
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
