class ColorSelector extends JPanel {
  private Rectangle primaryArea = new Rectangle(),
                    secondaryArea = new Rectangle(),
                    trCorner = new Rectangle(),
                    blCorner = new Rectangle();
  private JColorChooser primary, secondary;

  ColorSelector() {
    primary = new JColorChooser(Color.black);
    secondary = new JColorChooser(Color.white);
    primary.setPreviewPanel(new JPanel());
    secondary.setPreviewPanel(new JPanel());

    setPreferredSize(new Dimension(32, 32));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Point mousePos = e.getPoint();
        if (primaryArea.contains(mousePos)) {
          primary.createDialog((ColorSelector)e.getSource(), "Color Picker (Primary Color)" , true, primary, null, null).setVisible(true);
        } else if (secondaryArea.contains(mousePos)) {
          secondary.createDialog((ColorSelector)e.getSource(), "Color Picker (Secondary Color)" , true, secondary, null, null).setVisible(true);
        } else if (blCorner.contains(mousePos)) {
          primary.setColor(Color.black);
          secondary.setColor(Color.white);
        } else if (trCorner.contains(mousePos)) {
          Color temp = primary.getColor();
          primary.setColor(secondary.getColor());
          secondary.setColor(temp);
        } else {
          return;
        }
        repaint();
      }
    });
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    final Color foreground = new Color(0xADADAD);
    //color squares
    Graphics2D g2 = (Graphics2D) g;
    drawColorArea(g2, secondaryArea, Color.black, Color.white, secondary.getColor());
    drawColorArea(g2, primaryArea,  Color.black, Color.white, primary.getColor());

    g2.scale((double)getPreferredSize().width / 32d, (double)getPreferredSize().height / 32d);

    //default
    drawColorArea(g2, new Rectangle(3, 24, 7, 7), foreground, Color.white);
    drawColorArea(g2, new Rectangle(0, 21, 7, 7), foreground, Color.black);

    g.setColor(foreground);
    Polygon arrows = new Polygon(new int[] {28, 23, 23, 21, 23, 23, 28, 28, 26, 28, 30, 28, 28},
                                 new int[] {3, 3, 1, 3, 5, 3, 3, 8, 8, 10, 8, 8, 3},
                                 13);
    g2.translate(0, -32 / getPreferredSize().height + .2);
    g.fillPolygon(arrows);
    g.drawPolygon(arrows);
    g2.dispose();
  }

  private void drawColorArea(Graphics2D g, Rectangle area, Color... fill) {
    area = (Rectangle) area.clone();
    for (Color c: fill) {
      g.setPaint(c);
      g.fill(area);
      area.grow(-1, -1);
    }
  }

  @Override
  void setPreferredSize(Dimension size) {
    super.setPreferredSize(size);
    setMaximumSize(size);
    setMinimumSize(size);
    double scalex = size.width / 32d;
    double scaley = size.height / 32d;
    primaryArea.setRect(0, 0, 20d * scalex, 20d * scaley);
    secondaryArea.setRect(11d * scalex, 11d * scaley, 20d * scalex, 20d * scaley);
    trCorner.setRect(20d * scalex, 0, 12d * scalex, 12d * scaley);
    blCorner.setRect(0, 20d * scaley, 12d * scalex, 12d * scaley);
  }
}
