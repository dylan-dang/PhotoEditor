public class DocumentView extends JPanel {
  public final float[] ZOOM_TABLE = {2,3,4,5,6,7,8,9,10,12.5,17,20,25,33.33,50,66.67,100,150,200,300,400,500,600,800,1000,1200,1400,1600,2000,2400,3200,4000,4800,5600,6400};
  private final int INFOBAR_HEIGHT = 24;
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
  private JSpinner zoomSpinner;
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

    toolTipLabel = new JLabel((ImageIcon) view.getToolBar().getSelectedTool().getValue(Action.SMALL_ICON));
    toolTipLabel.setMinimumSize(new Dimension(0, INFOBAR_HEIGHT));

    Dimension labelSize = new Dimension(128, INFOBAR_HEIGHT);
    imageSizeLabel = new JLabel(String.format("%d x %dpx", document.getWidth(), document.getHeight()));
    imageSizeLabel.setIcon(infoBarIcon("imageSize.png"));
    imageSizeLabel.setPreferredSize(labelSize);
    imageSizeLabel.setMaximumSize(labelSize);

    positionLabel = new JLabel();
    positionLabel.setIcon(infoBarIcon("position.png"));
    positionLabel.setPreferredSize(labelSize);
    positionLabel.setMaximumSize(labelSize);

    setupInfoBar();
    setupViewport();
  }

  private ImageIcon infoBarIcon(String string) {
    return new ImageIcon(sketchPath(String.format("resources/infoBar/%s", string)));
  }

  private void setupInfoBar() {
    setupZoomControl();
    infoBar = new JPanel();
    infoBar.setLayout(new BoxLayout(infoBar, BoxLayout.X_AXIS));
    infoBar.setPreferredSize(new Dimension(0, INFOBAR_HEIGHT));
    infoBar.add(toolTipLabel);
    infoBar.add(Box.createGlue());
    addInfoSeparator();
    infoBar.add(imageSizeLabel);
    addInfoSeparator();
    infoBar.add(positionLabel);
    addInfoSeparator();
    infoBar.add(zoomSpinner);
    infoBar.add(new JLabel("%"));
    infoBar.add(fitToWindow);
    infoBar.add(zoomOut);
    infoBar.add(zoomSlider);
    infoBar.add(zoomIn);
    add(infoBar, BorderLayout.SOUTH);
  }
  private void addInfoSeparator() {
    JSeparator separator = new JSeparator(JSeparator.VERTICAL);
    Dimension size = new Dimension(separator.getPreferredSize().width, separator.getMaximumSize().height);
    separator.setMaximumSize(size);
    infoBar.add(separator);
    infoBar.add(Box.createRigidArea(new Dimension(5, INFOBAR_HEIGHT)));
  }

  private void setupZoomControl() {
    JButton[] buttons = {
      fitToWindow = new JButton(new ZoomToWindowAction(view)),
      zoomOut = new JButton(new ZoomOutAction(view)),
      zoomIn = new JButton(new ZoomInAction(view))
    };
    for(JButton button: buttons) {
      Dimension buttonSize = new Dimension(24, INFOBAR_HEIGHT);
      button.setText(null);
      button.setEnabled(true);
      button.setPreferredSize(buttonSize);
      button.setMinimumSize(buttonSize);
      button.setMaximumSize(buttonSize);
      button.setBorderPainted(false);
      button.setBackground(null);
      button.setOpaque(false);
    }

    fitToWindow.setIcon(infoBarIcon("fitToWindow.png"));
    zoomOut.setIcon(infoBarIcon("zoomOut.png"));
    zoomIn.setIcon(infoBarIcon("zoomIn.png"));

    zoomSpinner = new JSpinner(new SpinnerNumberModel(100d, 1d, 6400d, 1d));
    Dimension zoomSpinnerSize = new Dimension(45, INFOBAR_HEIGHT);
    JSpinner.NumberEditor editor = new JSpinner.NumberEditor(zoomSpinner, "##0.##");
    editor.getTextField().setBackground(null);
    zoomSpinner.setUI(new BasicSpinnerUI() {
      protected Component createNextButton() {
        return null;
      }
      protected Component createPreviousButton() {
        return null;
      }
    });
    zoomSpinner.setMaximumSize(editor.getTextField().getPreferredSize());
    zoomSpinner.setEditor(editor);
    zoomSpinner.setBorder(null);
    zoomSpinner.setBackground(null);
    zoomSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        float value = ((Number)zoomSpinner.getValue()).floatValue();
        setScale(value / 100);
      }
    });

    int i;
    for(i = 0; i < ZOOM_TABLE.length; i++) {
      if (ZOOM_TABLE[i] == 100) break;
    }
    zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, ZOOM_TABLE.length - 1, i);
    Dimension sliderSize = new Dimension(110, INFOBAR_HEIGHT);
    zoomSlider.setPreferredSize(sliderSize);
    zoomSlider.setMinimumSize(sliderSize);
    zoomSlider.setMaximumSize(sliderSize);
    zoomSlider.setSnapToTicks(true);
    zoomSlider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (source.getValueIsAdjusting())
        setScale(ZOOM_TABLE[source.getValue()] / 100);
      }
    });
  }

  private void setupViewport() {
    canvasWrapper = new JPanel(new GridBagLayout());
    canvasWrapper.add(canvas = new Canvas());
    canvasWrapper.setBackground(view.CONTENT_BACKGROUND);

    CanvasMouseListener canvasMouseListener = new CanvasMouseListener();
    canvas.addMouseWheelListener(canvasMouseListener);
    canvas.addMouseMotionListener(canvasMouseListener);
    canvas.addMouseListener(canvasMouseListener);
    canvasWrapper.addMouseMotionListener(canvasMouseListener);
    canvasWrapper.addMouseWheelListener(canvasMouseListener);
    canvasWrapper.addMouseListener(canvasMouseListener);

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

  private class CanvasMouseListener extends MouseAdapter {
    Point2D pos = new Point2D.Double(0, 0);
    ToolAction tool;
    DragGesture dragState;
    Point origin;

    @Override
    void mouseWheelMoved(MouseWheelEvent e) {
      if (e.isControlDown()) {
        setScale(constrain(scale * pow(1.1, -e.getWheelRotation()), .01, 64), e.getPoint());
        return;
      }
      Rectangle view = viewport.getViewRect();
      int delta = e.getWheelRotation() * 50;
      if (e.isShiftDown()) {
        view.x += delta;
      } else {
        view.y += delta;
      }

      ((JPanel)viewport.getView()).scrollRectToVisible(view);
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
      toolTipLabel.setText(tool.getToolTip());
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

  private class SelectionAnimator extends Thread {
    DocumentView docView;
    SelectionAnimator(DocumentView docView) {
      this.docView = docView;
    }
    public void run() {
      int cycle = 0;
      while(docView.hasSelection()) {
        Canvas canvas = docView.getCanvas();
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

  public void setCanvasBackground(Color c) {
    canvasWrapper.setBackground(c);
  }

  public float getScale() {
    return scale;
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
        )
      );
    }

    int lastTick = 0;
    while (lastTick < ZOOM_TABLE.length && scale >= ZOOM_TABLE[lastTick] / 100) {
      lastTick++;
    }
    if (!zoomSlider.getValueIsAdjusting())
      zoomSlider.setValue(lastTick);
    zoomSpinner.setValue(scale * 100);

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

  public Document getDocument() {
    return document;
  }

  public JViewport getViewport() {
    return viewport;
  }

  public Canvas getCanvas() {
    return canvas;
  }

  public void setToolTipText(String text) {
    toolTipLabel.setText(text);
  }

  public void setToolTipIcon(Icon icon) {
    toolTipLabel.setIcon(icon);
  }
}
