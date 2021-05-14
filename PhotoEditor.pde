import java.util.*;
import java.util.concurrent.CountDownLatch;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import java.awt.geom.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.plaf.basic.BasicToolBarUI;
import javax.swing.UIManager.*;
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

Set<Integer> pressedKeys = new HashSet<Integer>();

void setup() {
  //setup the frame
  size(800, 600);
  System.setProperty("apple.laf.useScreenMenuBar", "true"); //menubar to to top, for macs
  JFrame frame = (JFrame) ((processing.awt.PSurfaceAWT.SmoothCanvas) surface.getNative()).getFrame(); //hook into Processing frame
  frame.setTitle("Photo Editor");
  frame.setResizable(true);

  try {
    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      if ("Nimbus".equals(info.getName())) {
        //set look and feel to nimbus
        UIManager.setLookAndFeel(info.getClassName());
        break;
      }
    }
  } catch (Exception e) {
    println("couldn't load nimbus, defaulting to metal");
  }

  //add view to frame
  View view = new View(frame);
  
  //register keypresses to set
  KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
      switch (keyEvent.getID()) {
        case KeyEvent.KEY_PRESSED: pressedKeys.add(keyEvent.getKeyCode()); break;
        case KeyEvent.KEY_RELEASED: pressedKeys.remove(keyEvent.getKeyCode()); break;
      }
      return false;
    }
  });

  //set icon
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