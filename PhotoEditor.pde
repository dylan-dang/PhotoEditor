import java.util.*;
import java.util.concurrent.CountDownLatch;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.event.*;
import javax.imageio.*;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

void setup() {
  //styling
  com.formdev.flatlaf.FlatDarkLaf.install();

  //hook into Processing frame and setup
  size(800, 600);
  System.setProperty("apple.laf.useScreenMenuBar", "true");
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
