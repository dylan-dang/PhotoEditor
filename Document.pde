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
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong when trying to read your file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
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
    
    public BufferedImage flattened() {
        if (flattened == null) updateFlattenedCache();
        return flattened;
    }
    
    public void updateFlattenedCache() {
        //a real bottleneck for performance but it'll have to do for now, perhaps cache the preflattened layer below
        flattened = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = flattened.createGraphics();
        for (Layer layer : layers) {
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
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public Dimension getDimension() {
        return new Dimension(width, height);
    }
    
    public Layer addEmptyLayer(int index) {
        Layer layer = new Layer(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        layer.setName(String.format("Layer %d", layers.size()));
        layers.add(index, layer);
        return layer;
    }
    
    public Layer addEmptyLayer() {
        return addEmptyLayer(layers.size());
    }
    
    public void addLayer(Layer layer, int index) {
        layers.add(index, layer);
    }
    
    public void addLayer(Layer layer) {
        layers.add(layer);
    }
    
    public void crop(Rectangle rect) {
        for (Layer layer : layers) {
            layer.crop(rect);
        }
        width = rect.width;
        height = rect.height;
    }
}

public class SnapShot {
    private ArrayList<Layer> layers = new ArrayList<Layer>();
    private int height, width;
    private int selectedLayer;
    
    SnapShot(Document doc, int selectedLayer) {
        height = doc.getHeight();
        width = doc.getWidth();
        for (Layer layer : doc.getLayers()) {
            layers.add(layer.copy());
        }
        this.selectedLayer = selectedLayer;
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
    
    public int getSelectedLayer() {
        return selectedLayer;
    }
}

public class SnapShotManager {
    Document doc;
    DocumentView docView;
    ArrayDeque<SnapShot> undoHistory = new ArrayDeque<SnapShot>();
    ArrayDeque<SnapShot> redoHistory = new ArrayDeque<SnapShot>();
    
    SnapShotManager(DocumentView docView) {
        this.docView = docView;
        this.doc = docView.getDocument();
    }
    
    public void save(SnapShot snapshot) {
        redoHistory.clear();
        undoHistory.push(snapshot);
    }
    
    public void save() {
        redoHistory.clear();
        undoHistory.push(new SnapShot(doc, docView.getSelectedLayerIndex()));
        println(undoHistory);
    }
    
    public void undo() {
        if (!ableToUndo())return;
        SnapShot save = undoHistory.pop();
        redoHistory.push(new SnapShot(doc, docView.getSelectedLayerIndex()));
        restore(save);
    }
    
    public void redo() {
        if (!ableToRedo()) return;
        SnapShot save = redoHistory.pop();
        undoHistory.push(new SnapShot(doc, docView.getSelectedLayerIndex()));
        restore(save);
    }
    
    private void restore(SnapShot save) {
        ArrayList<Layer> layers = doc.getLayers();
        layers.clear();
        layers.addAll(save.getLayers());
        doc.setHeight(save.getHeight());
        doc.setWidth(save.getWidth());
        docView.setSelectedLayerIndex(save.getSelectedLayer());
        docView.revalidate();
        docView.repaint();
    }
    
    public boolean ableToUndo() {
        return !undoHistory.isEmpty();
    }
    
    public boolean ableToRedo() {
        return !redoHistory.isEmpty();
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
    
    public void crop(Rectangle rect) {
        g.dispose();
        BufferedImage crop = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
        image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        g.drawImage(crop, 0, 0, null);
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
    
    public Layer copy() {
        ColorModel colorModel = image.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
        BufferedImage imageCopy = new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
        Layer copy = new Layer(imageCopy);
        copy.setName(name);
        copy.setOpacity(opacity);
        copy.setVisible(isVisible());
        copy.setBlendComposite(blendIndex);
        return copy;
    }
}
