import java.awt.event.*;
import javax.swing.event.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;

JFrame frame;

void setup() {
  //styling
  com.formdev.flatlaf.FlatDarkLaf.install();

  //hook into Processing frame and setup
  size(800, 600); //easier than setting frame size and location
  frame = (JFrame) ((processing.awt.PSurfaceAWT.SmoothCanvas) surface.getNative()).getFrame();
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  frame.setResizable(true);
  frame.setTitle("Photo Editor");
  View view = new View(frame);
  Controller controller = new Controller(view);

  //temporary icon
  surface.setIcon(loadImage("https://image.flaticon.com/icons/png/512/196/196278.png"));

  noLoop();

}
