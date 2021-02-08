import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.swing.undo.UndoManager;

public class Model {
  ArrayList<Layer> layers = new ArrayList<Layer>();
  UndoManager undoManager = new UndoManager();
  int height, width;
  Model() {
    layers.add(new Layer());
  }
}


public class Layer {
  BufferedImage image;
  private boolean visible;
  private int opacity;
  //TODO blendmodes
  Layer() {
    visible = true;
    opacity = 0xFF;
  }
}
