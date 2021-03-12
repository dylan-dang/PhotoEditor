public class DocumentView extends JPanel {
  public final float[] ZOOM_TABLE = {2,3,4,5,6,7,8,9,10,12.5,17,20,25,33.33,50,66.67,100,150,200,300,400,500,600,800,1000,1200,1400,1600,2000,2400,3200,4000,4800,5600,6400};
  private final int INFOBAR_HEIGHT = 24;
  private final String INFOBAR_RESOURCE_PATH = "resources/infoBar/%s";
  private Thread selectionAnimator;
  private boolean drawPixelGrid = false;
  private float scale = 1;

  private View view;
  private Document document;
  private Layer selectedLayer;
  private Shape selection;

  private JPanel infoBar;
  private JLabel toolTipLabel;
  private JLabel imageSizeLabel;
  private JLabel positionLabel;
  private JButton fitToWindow, zoomOut, zoomIn;
  private JSlider zoomSlider;

  private JScrollPane scrollPane;
  private JViewport viewport;
  private JPanel canvasWrapper;
  private Canvas canvas;


  DocumentView(Document document, View view) {
    this.view = view;
    this.document = document;
    this.selectedLayer = document.getLayers().get(0);
    setLayout(new BorderLayout());

    toolTipLabel = new JLabel();
    imageSizeLabel = new JLabel(String.format("%d x %dpx", document.getWidth(), document.getHeight()));
    imageSizeLabel.setIcon(new ImageIcon(sketchPath(String.format(INFOBAR_RESOURCE_PATH, "imageSize.png"))));
    positionLabel = new JLabel(new ImageIcon(sketchPath(String.format(INFOBAR_RESOURCE_PATH, "position.png"))));


    Dimension sliderSize = new Dimension(110, INFOBAR_HEIGHT);
    int i;
    for(i = 0; i < ZOOM_TABLE.length; i++) {
      if (ZOOM_TABLE[i] == 100) break;
    }
    zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, ZOOM_TABLE.length - 1, i);
    zoomSlider.setSnapToTicks(true);
    zoomSlider.setPreferredSize(sliderSize);
    zoomSlider.setMinimumSize(sliderSize);
    zoomSlider.setMaximumSize(sliderSize);
    zoomSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (source.getValueIsAdjusting())
        setScale(ZOOM_TABLE[source.getValue()] / 100);
      }
    });

    setupInfoBar();
    setupScrollingCanvas();

    CanvasMouseListener canvasMouseListener = new CanvasMouseListener();
    canvas.addMouseWheelListener(canvasMouseListener);
    canvas.addMouseMotionListener(canvasMouseListener);
    canvas.addMouseListener(canvasMouseListener);
    canvasWrapper.addMouseMotionListener(canvasMouseListener);
    canvasWrapper.addMouseWheelListener(canvasMouseListener);
    canvasWrapper.addMouseListener(canvasMouseListener);
  }

  private void setupScrollingCanvas() {
    canvasWrapper = new JPanel(new GridBagLayout());
    canvasWrapper.add(canvas = new Canvas());
    canvasWrapper.setBackground(view.CONTENT_BACKGROUND);

    scrollPane = new JScrollPane();
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    add(scrollPane, BorderLayout.CENTER);

    viewport = scrollPane.getViewport();
    viewport.add(canvasWrapper);
    viewport.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        viewport.repaint();
      }
    });
  }

  private void setupInfoBar() {
    infoBar = new JPanel();
    infoBar.setLayout(new BoxLayout(infoBar, BoxLayout.X_AXIS));
    infoBar.setPreferredSize(new Dimension(0, INFOBAR_HEIGHT));
    infoBar.add(toolTipLabel);
    infoBar.add(Box.createGlue());
    infoBar.add(imageSizeLabel);
    infoBar.add(positionLabel);
    infoBar.add(new JSpinner());
    infoBar.add(zoomSlider);
    add(infoBar, BorderLayout.SOUTH);
  }

  private class CanvasMouseListener extends MouseAdapter {
    Point2D pos = new Point2D.Double(0, 0);
    ToolAction tool;
    DragGesture dragState;
    Point origin;

    @Override
    void mouseWheelMoved(MouseWheelEvent e) {
      setScale(constrain(scale * pow(1.1, -e.getWheelRotation()), .01, 64), e.getPoint());
    }
    @Override
    void mouseMoved(MouseEvent e) {
      updatePos(e);
      updatePostionLabel();
    }

    @Override
    void mouseDragged(MouseEvent e) {
      updatePos(e);
      dragState.dragTo(pos);
      tool.dragging();
      canvas.repaint();
      updatePostionLabel();
    }
    @Override
    void mousePressed(MouseEvent e) {
      origin = e.getPoint();
      updatePos(e);
      if (!dragState.isDragging()) {
        dragState.start(pos, e.getButton());
      } else {
        dragState.pressButton(e.getButton());
      }
      tool.dragStarted();
      tool.dragging();
    }

    @Override
    void mouseReleased(MouseEvent e) {
      updatePos(e);
      dragState.releaseButton(e.getButton());
      if (dragState.getButtons().isEmpty()) {
        dragState.stop(pos);
      }
      tool.dragEnded();
    }

    void updatePos(MouseEvent e) {
      Point mouse = e.getPoint();
      tool = view.getToolBar().getSelectedTool();
      dragState = tool.getDragState();
      JComponent source = (JComponent)e.getSource();
      if(source.getLayout() instanceof GridBagLayout) { //wrapper
        Component canvas = source.getComponent(0);
        mouse.translate(-(source.getWidth()-canvas.getWidth()) / 2, -(source.getHeight()-canvas.getHeight()) / 2);
      }
      pos.setLocation(mouse.x / scale, mouse.y / scale);
    }

    @Override
    void mouseClicked(MouseEvent e) {
      updatePos(e);
      tool.click(pos, e.getButton());
    }

    private void updatePostionLabel() {
      positionLabel.setText(String.format("%d, %dpx", (int) pos.getX(), (int) pos.getY()));
    }
  }

  private class Canvas extends JPanel {
    final Stroke singleWhiteDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 0);
    final Stroke singleBlackDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1}, 1);
    public Stroke selectionWhiteDash = new BasicStroke();
    public Stroke selectionBlackDash = new BasicStroke();

    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g.create();

      Rectangle viewRect = viewport.getViewRect();
      int deltax = viewRect.x % 16;
      int deltay = viewRect.y % 16;
      DrawHelper.drawChecker(g2, viewRect.x - deltax, viewRect.y - deltay, viewRect.width + deltax - 1, viewRect.height + deltay - 1, 8);
      AffineTransform preScale = g2.getTransform();
      g2.scale(scale, scale);
      g2.drawImage(document.flattened(), 0, 0, null);
      g2.setTransform(preScale);

      if (drawPixelGrid && (viewRect.height + viewRect.width) / scale < 50) { //have to limit at 50 lines idk how to make it faster
        int startingRow = int(viewRect.y / scale);
        for(int row = startingRow + 1; row < startingRow + viewRect.height / scale + 1; row ++) {
          int y = (int)(scale * row);
          drawDoubleDashed(g2, new Line2D.Float(viewRect.x, y, viewRect.x + viewRect.width, y));
        }
        int startingCol = int(viewRect.x / scale);
        for(int col = startingCol + 1; col < startingCol + viewRect.width / scale + 1; col ++) {
          int x = (int)(scale * col);
          drawDoubleDashed(g2, new Line2D.Float(x, viewRect.y, x, viewRect.y + viewRect.height));
        }
      }

      //hacky way of showing selection on bottom and right edges consistently
      g2.setColor(view.CONTENT_BACKGROUND);
      Dimension size = getPreferredSize();
      g2.drawLine(--size.width, 0, size.width, --size.height);
      g2.drawLine(0, size.height, size.width, size.height);


      if (hasSelection()) {
        drawDoubleDashed(g2, getScaledSelection(), selectionWhiteDash, selectionBlackDash);
      }
      g2.dispose();
    }

    private void drawDoubleDashed(Graphics2D g2, Shape shape, Stroke white, Stroke black) {
      g2.setColor(Color.white);
      g2.setStroke(white);
      g2.draw(shape);
      g2.setColor(Color.black);
      g2.setStroke(black);
      g2.draw(shape);
    }
    private void drawDoubleDashed(Graphics2D g2, Shape shape) {
      drawDoubleDashed(g2, shape, singleWhiteDash, singleBlackDash);
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(
        round(document.getWidth() * scale + 1),
        round(document.getHeight() * scale + 1)
      );
    }
    public boolean largerThan(Dimension container) {
      return getPreferredSize().width > container.getWidth() ||
             getPreferredSize().height > container.getHeight();
    }
  }

  public void setCanvasBackground(Color c) {
    canvasWrapper.setBackground(c);
  }

  public float getScale() {
    return scale;
  }

  public Document getDocument() {
    return document;
  }

  public void setScale(float scale, Point2D pos) {
    if(pos == null) {
      setScale(scale);
      return;
    }

    float deltaScale = scale / this.scale;

    this.scale = scale;
    //if canvas is smaller than viewport, no need to translate the view position
    if(canvas.largerThan(viewport.getExtentSize())) {
      Point viewPos = viewport.getViewPosition();
      viewport.setViewPosition(new Point(
        (int)Math.round(viewPos.x + pos.getX() * deltaScale - pos.getX()),
        (int)Math.round(viewPos.y + pos.getY() * deltaScale - pos.getY())
      ));
    }

    int lastTick = 0;
    while (lastTick < ZOOM_TABLE.length && scale >= ZOOM_TABLE[lastTick] / 100) {
      lastTick++;
    }
    if (!zoomSlider.getValueIsAdjusting())
    zoomSlider.setValue(lastTick);

    canvas.revalidate();
    viewport.repaint();
  }

  public void setScale(float scale) {
    //default to center of viewrect
    Rectangle viewRect = viewport.getViewRect();
    setScale(scale, new Point(viewRect.x + viewRect.width / 2, viewRect.y + viewRect.height / 2));
  }

  public Layer getSelectedLayer() {
    return selectedLayer;
  }

  public void setSelectedLayer(Layer layer) {
    selectedLayer = layer;
  }

  public JViewport getViewport() {
    return viewport;
  }

  public Canvas getCanvas() {
    return canvas;
  }

  public Shape getSelection() {
    if (selection == null) return new Rectangle2D.Double(0, 0, document.getWidth(), document.getHeight());
    return selection;
  }

  public Shape getScaledSelection() {
    if (selection == null) return new Rectangle2D.Double(0, 0, document.getWidth() * scale, document.getHeight() * scale);
    AffineTransform transform = new AffineTransform();
    transform.scale(scale, scale);
    return transform.createTransformedShape(selection);
  }

  public void setSelection(Shape selection) {
    this.selection = selection;
    canvas.repaint();
    if (selectionAnimator == null || !selectionAnimator.isAlive() && hasSelection()) {
      selectionAnimator = new SelectionAnimator(this);
      selectionAnimator.start();
    }
  }

  public boolean hasSelection() {
    return selection != null;
  }

  public void removeSelection() {
    this.selection = null;
  }
}

class SelectionAnimator extends Thread {
  DocumentView docView;
  SelectionAnimator(DocumentView docView) {
    this.docView = docView;
  }
  public void run() {
    int cycle = 0;
    while(docView.hasSelection()) {
      DocumentView.Canvas canvas = docView.getCanvas();
      canvas.selectionBlackDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 5 - cycle);
      canvas.selectionWhiteDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 5 - ((cycle + 3) % 6));
      Rectangle bounds = docView.getScaledSelection().getBounds();
      bounds.grow(1, 1); //confirm entire selection is repainted
      canvas.repaint(bounds);
      cycle = (cycle + 1) % 6;
      try {
        Thread.sleep(100);
      } catch(Exception e) {}
    }
  }
}
