private abstract class MenuBarAction extends AbstractAction {
  public MenuBarAction(String name, String accelerator) {
    putValue(NAME, name);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
  }
}

//File Menu Actions
public class NewFileAction extends MenuBarAction {
  Controller controller;
  public NewFileAction(Controller controller) {
    super("New...", "ctrl N");
    this.controller = controller;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    controller.addDocument(new Document(800, 600));
  }
}

public class OpenFileAction extends MenuBarAction {
  File file;
  View view;
  Controller controller;

  public OpenFileAction(Controller controller, View view) {
    super("Open...", "ctrl O");
    this.view = view;
    this.controller = controller;
  }
  public OpenFileAction(Controller controller) {
    this(controller, null);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().addAll(
       new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
       new ExtensionFilter("All Files", "*.*"));
    //stop the main thread and freeze frame until a file is chosen
    final CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(new Runnable() {
      @Override public void run() {
        if (view != null)
          view.getFrame().setEnabled(false);
        file = fileChooser.showOpenDialog(null);
        latch.countDown();
      }
    });
    try {
      latch.await();
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    } finally {
      view.getFrame().setEnabled(true);
      //println(file);
      controller.addDocument(new Document(file));
    }
  }
}

public class SaveAction extends MenuBarAction {
  public SaveAction() {
    super("Save", "ctrl S");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //TODO create action
  }
}

public class SaveAsAction extends MenuBarAction {
  public SaveAsAction() {
    super("Save As...", "ctrl shift S");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
  }
}

public class CloseFileAction extends MenuBarAction {
  public CloseFileAction() {
    super("Close", "ctrl W");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //TODO create action
  }
}

public class CloseAllAction extends MenuBarAction {
  public CloseAllAction() {
    super("Close All", "ctrl alt W");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //TODO create action
  }
}

public class CloseOtherAction extends MenuBarAction {
  public CloseOtherAction() {
    super("Close Others", "ctrl alt P");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //TODO create action
  }
}

public class PrintAction extends MenuBarAction {
  public PrintAction() {
    super("Print...", "ctrl P");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //TODO create action
  }
}

public class ExitAction extends MenuBarAction {
  public ExitAction() {
    super("Exit", "ctrl Q");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //TODO create action
  }
}
