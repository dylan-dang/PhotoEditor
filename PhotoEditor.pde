import java.awt.event.*;
import javax.swing.event.*;
import java.awt.*;
import javax.swing.*;
import javax.imageio.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.util.Vector;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

void setup() {
  //styling
  com.formdev.flatlaf.FlatDarkLaf.install();

  //hook into Processing frame and setup
  size(800, 600);
  JFrame frame = (JFrame) ((processing.awt.PSurfaceAWT.SmoothCanvas) surface.getNative()).getFrame();
  frame.setTitle("Photo Editor");
  frame.setResizable(true);
  View view = new View(frame);

  //temporary icon
  surface.setIcon(loadImage("https://image.flaticon.com/icons/png/512/196/196278.png"));

  noLoop();
}

void exit() {}

void forceExit() {
  super.exit();
}
