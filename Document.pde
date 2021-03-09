public class Document {
  ArrayList<Layer> layers = new ArrayList<Layer>();
  private int height, width;
  private String name = "Untitled";
  private boolean isSaved = true;
  private BufferedImage flattened;
  private File linkedFile;

  Document(int width, int height) {
    this.width = 1601;
    this.height = 664;
    BufferedImage image = (BufferedImage)loadImage("https://helpx.adobe.com/content/dam/help/en/photoshop/using/convert-color-image-black-white/jcr_content/main-pars/before_and_after/image-before/Landscape-Color.jpg").getImage();
    image = toRGBA(image);
    layers.add(new Layer(image));

    try {
      layers.add(new Layer(toRGBA(ImageIO.read(new File("C:/Users/user1/Desktop/smile.png")))));
    } catch (IOException e) {
      return; //TODO dialog error
    }
  }
  Document(File file) {
    linkedFile = file;
    BufferedImage image;
    try {
      image = ImageIO.read(file);
    } catch (IOException e) {
      return; //TODO dialog error
    }
    width = image.getWidth();
    height = image.getHeight();
    layers.add(new Layer(toRGBA(image)));
  }
  Document(BufferedImage image) {
    this.width = image.getWidth();
    this.height = image.getHeight();
    layers.add(new Layer(toRGBA(image)));
  }
  private BufferedImage toRGBA(BufferedImage image) {
    BufferedImage rbgaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = rbgaImage.createGraphics();
    g2.drawImage(image, null, 0, 0);
    g2.dispose();
    return rbgaImage;
  }
  public String getName() {
    return name;
  }

  public BufferedImage getFlattenedView() {
    if (flattened == null) updateFlattenedView();
    return flattened;
  }
  public void updateFlattenedView() { //we need to cache the view for performance
    flattened = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = flattened.createGraphics();
    g2.drawImage(layers.get(0).getImage(), null, 0, 0);
    for(int i = 1; i < layers.size(); i++) {
      final Layer layer = layers.get(i);
      if (layer.isVisible()) {
        BlendComposite blendComposite = layer.getBlendComposite();
        blendComposite.setOpacity(layer.getOpacity());
        g2.setComposite(blendComposite);
        g2.drawImage(layer.getImage(), null, 0, 0);
      }
    }
    g2.dispose();
  }

  public boolean isSaved() {
    return isSaved;
  }
  public ArrayList<Layer> getLayers() {
    return layers;
  }
  public int getHeight() {
    return height;
  }
  public int getWidth() {
    return width;
  }
  public Layer addLayer(int index) {
    Layer layer = new Layer(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
    layers.add(index, layer);
    return layer;
  }
  public Layer addLayer() {
    return addLayer(layers.size());
  }
}

public class Layer {
  BufferedImage image;
  private String name;
  private boolean visibility = true;
  private float opacity = 1f;
  private int blendIndex = 0;

  Layer(BufferedImage image) {
    if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
      throw new IllegalStateException("Expected TYPE_INT_ARGB");
    }
    this.image = image;
  }

  public BufferedImage getImage() {
    return image;
  }
  public BlendComposite getBlendComposite() {
    return BLEND_MODES[blendIndex];
  }
  public void setBlendComposite(int index) {
    blendIndex = index;
  }
  public boolean isVisible() {
    return visibility;
  }
  public void setVisible(boolean visibility) {
    this.visibility = visibility;
  }
  public float getOpacity() {
    return opacity;
  }
  public void setOpacity(float opacity) {
    this.opacity = Math.max(0, Math.min(1, opacity));
  }
}
