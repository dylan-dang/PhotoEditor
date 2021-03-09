public static class DrawHelper {
  public final static Color CHECKER = new Color(0xCCCCCC);

  public static void drawChecker(Graphics2D g, int x, int y, int width, int height, int size) {
    g.setColor(Color.white);
    g.fillRect(x, y, width, height);
    g.setColor(new Color(0xCCCCCC));
    for(int i = 0; i < height; i += size) {
      for(int j = 0; j < width; j += size * 2) {
        int squareX = x + j + i % (2 * size);
        int squareY = y + i;
        g.fillRect(squareX, squareY, Math.max(0, size - Math.max(0, squareX + size - x - width)), Math.max(0, size - Math.max(0, squareY + size - y - height)));
      }
    }
  }

  public static void drawBorderedArea(Graphics2D g, Rectangle area, Color... fill) {
    area = (Rectangle) area.clone();
    area.width--;
    area.height--;
    for (Color c: fill) {
      g.setPaint(c);
      g.draw(area);
      area.grow(-1, -1);
    }
    area.width++;
    area.height++;
    g.fill(area);
  }
}
