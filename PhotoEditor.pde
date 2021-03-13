import java.util.*;
import java.util.concurrent.CountDownLatch;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.event.*;
import javax.imageio.*;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

void setup() {
  //styling (isn't necessary to run the program but sets up the theme)
  com.formdev.flatlaf.FlatDarkLaf.install();

  size(800, 600);
  System.setProperty("apple.laf.useScreenMenuBar", "true"); //menubar to to top, for macs
  JFrame frame = (JFrame) ((processing.awt.PSurfaceAWT.SmoothCanvas) surface.getNative()).getFrame(); //hook into Processing frame
  frame.setTitle("Photo Editor");
  frame.setResizable(true);

  View view = new View(frame);

  //temporary icon
  surface.setIcon(loadImage("https://image.flaticon.com/icons/png/512/196/196278.png"));

  //don't wast resources on a drawing Thread.
  noLoop();
}

//override the default exit function so we can intercept and ask the use to save when they exit
@Override
void exit() {}

void forceExit() {
  //used to actually exit the program
  super.exit();
}
