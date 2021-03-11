public class Document {
  ArrayList<Layer> layers = new ArrayList<Layer>();
  private int height, width;
  private boolean isSaved = true;
  private BufferedImage flattened;
  private File linkedFile;

  Document(int width, int height) {
    this.width = width;
    this.height = height;
    Layer layer = new Layer(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
    layers.add(layer);
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
    if (linkedFile != null) {
      return linkedFile.getName();
    }
    return "Untitled";
  }

  public BufferedImage getFlattenedView() {
    if (flattened == null) updateFlattenedView();
    return flattened;
  }
  public void updateFlattenedView() { //we need to cache the view for performance
    flattened = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = flattened.createGraphics();
    for(Layer layer: layers) {
      if (!layer.isVisible()) continue;
      BlendComposite blendComposite = layer.getBlendComposite();
      blendComposite.setOpacity(layer.getOpacity());
      g2.setComposite(blendComposite);
      g2.drawImage(layer.getImage(), null, 0, 0);
    }
    g2.dispose();
  }

  public boolean isSaved() {
    return isSaved;
  }
  public void setSaved(boolean value) {
    isSaved = value;
  }
  public boolean isLinked() {
    return linkedFile != null;
  }
  public File getLinkedFile() {
    return linkedFile;
  }
  public void setLinkedFile(File file) {
    this.linkedFile = file;
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
  public Dimension getDimension() {
    return new Dimension(width, height);
  }
  public Layer addLayer(int index) {
    Layer layer = new Layer(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
    layer.setName(String.format("Layer %d", layers.size()));
    layers.add(index, layer);
    return layer;
  }
  public Layer addLayer() {
    return addLayer(layers.size());
  }
}

public class Layer {
  BufferedImage image;
  private String name = "Background";
  private boolean visibility = true;
  private float opacity = 1f;
  private Graphics2D g;
  private int blendIndex = 0;

  Layer(BufferedImage image) {
    if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
      throw new IllegalStateException("Expected TYPE_INT_ARGB");
    }
    this.image = image;
    g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  }

  public BufferedImage getImage() {
    return image;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public BlendComposite getBlendComposite() {
    return BLEND_MODES[blendIndex];
  }
  public int getBlendIndex() {
    return blendIndex;
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

  public Graphics2D getGraphics() {
    return g;
  }
}
