public class Document {
  ArrayList<Layer> layers = new ArrayList<Layer>();
  private int height, width;
  private String name = "Untitled";
  private boolean isSaved = false;

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
  public String getName() {
    return name;
  }

  BufferedImage getFlattenedView() {
    return layers.get(0).getLayerData();
  }

  public boolean isSaved() {
    return isSaved;
  }
  public ArrayList getLayers() {
    return layers;
  }

}

public class Layer {
  BufferedImage image;
  private String name;
  private boolean visible;
  private int opacity;
  //TODO blendmodes

  Layer() {
    image = (BufferedImage)loadImage("https://helpx.adobe.com/content/dam/help/en/photoshop/using/convert-color-image-black-white/jcr_content/main-pars/before_and_after/image-before/Landscape-Color.jpg").getImage();
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
