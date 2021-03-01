import java.util.ArrayList;
import java.awt.image.BufferedImage;

public class Document {
  ArrayList<Layer> layers = new ArrayList<Layer>();
  int height, width;
  private String name = "new image";

  Document(int width, int height) {
    this.width = width;
    this.height = height;
    layers.add(new Layer());
  }
  Document(BufferedImage image) {
    this.height = image.getWidth();
    this.width = image.getHeight();
    layers.add(new Layer(image));
  }
  Document(File file) {
    try {
      BufferedImage image = ImageIO.read(file);
      layers.add(new Layer(image));
      this.height = image.getWidth();
      this.width = image.getHeight();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  String getName() {
    return name;
  }

  BufferedImage getEditView() {
    return layers.get(0).getLayerData();
  }

  public class Layer {
    BufferedImage image;
    private String name;
    private boolean visible;
    private int opacity;
    //TODO blendmodes

    Layer() {
      this.image = new BufferedImage(800, 600, 1);
    }

    Layer(BufferedImage image) {
      this.image = image;
      visible = true;
      opacity = 0xFF;
    }

    BufferedImage getLayerData() {
      return image;
    }
  }
}