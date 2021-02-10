import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.swing.undo.UndoManager;

public class Document {
  ArrayList<Layer> layers = new ArrayList<Layer>();
  UndoManager undoManager = new UndoManager();
  int height, width;
  Document() {
    layers.add(new Layer());
  }

  void restore(Snapshot memento) {}
}


public class Layer {
  BufferedImage image;
  private String name;
  private boolean visible;
  private int opacity;
  //TODO blendmodes
  Layer() {
    visible = true;
    opacity = 0xFF;
  }
  void commit() {}

  void commitAll() {}
}

private class Snapshot {
  private Document document;
  Snapshot(Document document) {
    this.document = document;
  }
}
