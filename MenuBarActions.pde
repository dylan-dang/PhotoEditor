//Super Menu bar action
private abstract class MenuBarAction extends AbstractAction {
  View view;

  public MenuBarAction(View view, String name, String accelerator) {
    this.view = view;
    putValue(NAME, name);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
    //setEnabled(false);
  }

  public MenuBarAction(View view, String name) {
    this(view, name, null);
  }

  public void execute() {
    actionPerformed(new ActionEvent(view.getFrame(), ActionEvent.ACTION_FIRST, null));
  }

  private File file; //hacky, i know
  public File promptFile(final boolean isOpen) {
    final FileChooser fileChooser = new FileChooser();
    if (isOpen) fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Image Types", "*.png", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif", "*.bmp", ".dib", "*.wbmp", "*.gif"));
    fileChooser.getExtensionFilters().addAll(
       new ExtensionFilter("PNG", "*.png"),
       new ExtensionFilter("JPEG", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif"),
       new ExtensionFilter("BMP", "*.bmp", ".dib"),
       new ExtensionFilter("WBMP", "*.wbmp"),
       new ExtensionFilter("GIF", "*.gif"),
       new ExtensionFilter("All Files", "*.*"));
    //stop the main thread and freeze frame until a file is chosen
    final CountDownLatch latch = new CountDownLatch(1);
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        view.getFrame().setEnabled(false);
        file = isOpen ? fileChooser.showOpenDialog(null) : fileChooser.showSaveDialog(null);
        latch.countDown();
        view.getFrame().setAlwaysOnTop(true);
      }
    });
    try {
      latch.await();
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    } finally {
      view.getFrame().setEnabled(true);
      view.getFrame().setAlwaysOnTop(false);
      return file;
    }
  }
}

//File Menu Actions
public class NewFileAction extends MenuBarAction {
  public NewFileAction(View view) {
    super(view, "New...", "ctrl N");
    setEnabled(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    view.addDocument(new Document(800, 600));
  }
}

public class OpenFileAction extends MenuBarAction {

  public OpenFileAction(View view) {
    super(view, "Open...", "ctrl O");
    setEnabled(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    File file = super.promptFile(true);
    if (file != null) view.addDocument(new Document(file));
  }
}

public class SaveAction extends MenuBarAction {
  public SaveAction(View view) {
    super(view, "Save", "ctrl S");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //TODO create action
  }
}

public class SaveAsAction extends MenuBarAction {
  public SaveAsAction(View view) {
    super(view, "Save As...", "ctrl shift S");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    promptFile(false);
  }
}

public class CloseFileAction extends MenuBarAction {
  private final String[] options = {"Save", "Don't Save", "Cancel"};
  private final int SAVE = 0, DONT_SAVE = 1, CANCEL = 2;
  private int index;
  private int response = 2;

  public CloseFileAction(View view, int index) {
    super(view, "Close", "ctrl W");
    this.index = index;
  }

  public CloseFileAction(View view) {
    this(view, -1);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    int i = index == -1 ? view.getImageTabs().getSelectedIndex() : index;
    JTabbedPane imageTabs = view.getImageTabs();
    DocumentView docView = (DocumentView) imageTabs.getComponentAt(i);
    if (!docView.getDocument().isSaved()) {
      response = JOptionPane.showOptionDialog(view.getFrame(),
        String.format("Save changes to %s before closing?", docView.getDocument().getName()),
        "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[SAVE]);
      switch(response) {
        case CANCEL: return;
        case SAVE: new SaveAction(view).execute();
      }
    } else {response = SAVE;}
    imageTabs.remove(i);
  }
  public boolean getSuccess() {
    return response != CANCEL;
  }
}

public class CloseAllAction extends MenuBarAction {
  public CloseAllAction(View view) {
    super(view, "Close All", "ctrl alt W");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final int tabCount = view.getImageTabs().getTabCount();
    for(int i = 0; i < tabCount; i++) {
      int lastTab = tabCount - i - 1;
      view.getImageTabs().setSelectedIndex(lastTab);
      CloseFileAction closeFileAction = new CloseFileAction(view, lastTab);
      closeFileAction.execute();
      if (!closeFileAction.getSuccess()) {
        break;
      }
    }
  }
}

public class CloseOtherAction extends MenuBarAction {
  public CloseOtherAction(View view) {
    super(view, "Close Others", "ctrl alt P");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //TODO create action
  }
}

public class PrintAction extends MenuBarAction {
  public PrintAction(View view) {
    super(view, "Print...", "ctrl P");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    //TODO create action
  }
}

public class ExitAction extends MenuBarAction {
  public ExitAction(View view) {
    super(view, "Exit", "ctrl Q");
    setEnabled(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    new CloseAllAction(view).execute();
    if (view.getImageTabs().getTabCount() == 0) {
      forceExit();
    }
  }
}
