public class DocumentView extends JPanel {
  private View view;
  private Document document;
  private JPanel infoBar = new JPanel();
  private final float[] funcTable = {2,3,4,5,6,7,8,9,10,12.5,17,20,25,33.33,50,66.67,100,150,200,300,400,500,600,800,1000,1200,1400,1600,2000,2400,3200,4000,4800,5600,6400};
  private JSlider slider;
  private float scale = 1;
  private JPanel canvasWrapper = new JPanel(new GridBagLayout());
  private Canvas canvas;
  private JScrollPane scrollPane = new JScrollPane();
  private JViewport viewport = scrollPane.getViewport();

  DocumentView(Document document, View view) {
    this.document = document;
    this.view = view;
    setLayout(new BorderLayout());
    canvasWrapper.add(canvas = new Canvas());

    infoBar.setLayout(new BoxLayout(infoBar, BoxLayout.X_AXIS));
    infoBar.add(Box.createGlue());
    infoBar.add(new JSpinner());

    slider = new JSlider(JSlider.HORIZONTAL, 0, funcTable.length - 1, funcTable.length / 2);
    //slider.setPaintTicks(true);
    //slider.setMajorTickSpacing(1);
    slider.setSnapToTicks(true);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        if (source.getValueIsAdjusting())
        setScale(funcTable[source.getValue()] / 100);
      }
    });

    infoBar.add(Box.createGlue());
    infoBar.add(slider);
    add(infoBar, BorderLayout.SOUTH);

    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
    canvasWrapper.setBackground(view.CONTENT_BACKGROUND);
    viewport.add(canvasWrapper);
    add(scrollPane, BorderLayout.CENTER);

    final View finalView = view;
    MouseAdapter mouseListener = new MouseAdapter() {
      Point2D pos = new Point2D.Double(0, 0);
      ToolAction tool;
      DragGesture dragState;

      @Override
      void mouseWheelMoved(MouseWheelEvent e) {
        setScale(constrain(scale * pow(1.1, -e.getWheelRotation()), .01, 64),
                 e.getSource() instanceof JViewport ? null : e.getPoint());
      }
      @Override
      void mouseMoved(MouseEvent e) {
        updatePos(e);
      }

      @Override
      void mouseDragged(MouseEvent e) {
        updatePos(e);
        dragState.dragTo(pos);
        tool.execute();
      }
      @Override
      void mousePressed(MouseEvent e) {
        updatePos(e);
        if (!dragState.isDragging()) {
          dragState.start(pos, e.getButton());
        } else {
          dragState.pressButton(e.getButton());
        }
        tool.execute();
      }
      @Override
      void mouseReleased(MouseEvent e) {
        updatePos(e);
        dragState.releaseButton(e.getButton());
        if (dragState.getButtons().isEmpty()) {
          dragState.stop(pos);
        }
      }
      void updatePos(MouseEvent e) {
        Point mouse = e.getPoint();
        tool = finalView.getSelectedTool();
        dragState = tool.getDragState();
        JComponent source = (JComponent)e.getSource();
        if(source.getLayout() instanceof GridBagLayout) { //wrapper
          Component canvas = source.getComponent(0);
          mouse.translate(-(source.getWidth()-canvas.getWidth()) / 2, -(source.getHeight()-canvas.getHeight()) / 2);
        }
        pos.setLocation(mouse.x / scale, mouse.y / scale);
      }
    };
    canvas.addMouseWheelListener(mouseListener);
    canvas.addMouseMotionListener(mouseListener);
    canvas.addMouseListener(mouseListener);
    canvasWrapper.addMouseMotionListener(mouseListener);
    canvasWrapper.addMouseWheelListener(mouseListener);
    canvasWrapper.addMouseListener(mouseListener);
  }

  private class Canvas extends JPanel {
    private final BufferedImage image = document.getFlattenedView();
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g.create();
      g2.scale(scale, scale);
      g2.drawImage(image, 0, 0, null);
      g2.dispose();
    }
    @Override
    public Dimension getPreferredSize() {
      return new Dimension(
        round(image.getWidth() * scale),
        round(image.getHeight() * scale)
      );
    }
    public boolean largerThan(Dimension container) {
      return image.getWidth() * scale > container.getWidth() ||
             image.getHeight() * scale > container.getHeight();
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

  public void setScale(float scale, Point pos) {
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
        viewPos.x + round(pos.x * deltaScale) - pos.x,
        viewPos.y + round(pos.y * deltaScale) - pos.y
      ));
    }

    int lastTick = 0;
    while (lastTick < funcTable.length && scale >= funcTable[lastTick] / 100) {
      lastTick++;
    }
    if (!slider.getValueIsAdjusting())
    slider.setValue(lastTick);

    canvas.revalidate();
    viewport.repaint();
  }

  public void setScale(float scale) {
    //default to center of viewrect
    Rectangle viewRect = viewport.getViewRect();
    setScale(scale, new Point(viewRect.x + viewRect.width / 2, viewRect.y + viewRect.height / 2));
  }

}
