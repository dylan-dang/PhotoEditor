import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class Controller {
  private View view;
  final JFXPanel jfxPanel = new JFXPanel();
  Controller controller = this;

  Controller(View view) {
    this.view = view;
    view.getFrame().addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        new ExitAction().execute();
      }
    });
    addAllMenuBarItems();
  }

  public void addAllMenuBarItems() {
    addMenuActions(view.getMenu("File"), new MenuBarAction[] {
      new NewFileAction(),
      new OpenFileAction(),
      null,
      new SaveAction(),
      new SaveAsAction(),
      null,
      new CloseFileAction(),
      new CloseAllAction(),
      new CloseOtherAction(),
      null,
      new PrintAction(),
      null,
      new ExitAction()
    });
    addMenuActions(view.getMenu("Edit"), new MenuBarAction[] {
      //TODO
    });
    addMenuActions(view.getMenu("View"), new MenuBarAction[] {
      //TODO
    });
    addMenuActions(view.getMenu("Image"), new MenuBarAction[] {
      //TODO
    });
    addMenuActions(view.getMenu("Layer"), new MenuBarAction[] {
      //TODO
    });
    addMenuActions(view.getMenu("Filter"), new MenuBarAction[] {
      //TODO
    });
  }

  private void addMenuActions(JMenu menu, MenuBarAction[] actions) {
    for(MenuBarAction action: actions) {
      if (action == null) {
        menu.addSeparator();
      } else {
        menu.add(action);
      }
    }
  }

  //Menu bar actions
  private abstract class MenuBarAction extends AbstractAction {
    public MenuBarAction(String name, String accelerator) {
      putValue(NAME, name);
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
      //setEnabled(false);
    }
    public MenuBarAction(String name) {
      this(name, null);
    }
    public void execute() {
      actionPerformed(new ActionEvent(view.getFrame(), ActionEvent.ACTION_FIRST, null));
    }
  }

  //File Menu Actions
  public class NewFileAction extends MenuBarAction {
    public NewFileAction() {
      super("New...", "ctrl N");
      setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      view.addDocument(new Document(800, 600));
    }
  }

  public class OpenFileAction extends MenuBarAction {

    public OpenFileAction() {
      super("Open...", "ctrl O");
      setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      File file = promptFile(true);
      if (file != null) view.addDocument(new Document(file));
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
      promptFile(false);
    }
  }

  public class CloseFileAction extends MenuBarAction {
    private final String[] options = {"Save", "Don't Save", "Cancel"};
    private final int SAVE = 0, DONT_SAVE = 1, CANCEL = 2;
    private int index;
    private int response = 2;

    public CloseFileAction(int index) {
      super("Close", "ctrl W");
      this.index = index;
    }

    public CloseFileAction() {
      this(-1);
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
          case SAVE: new SaveAction().execute();
        }
      } else {response = SAVE;}
      imageTabs.remove(i);
    }
    public boolean getSuccess() {
      return response != CANCEL;
    }
  }

  public class CloseAllAction extends MenuBarAction {
    public CloseAllAction() {
      super("Close All", "ctrl alt W");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      for(int i = 0; i < view.getImageTabs().getTabCount(); i++) {
        new CloseFileAction(i).execute();
      }
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
      setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      new CloseAllAction().execute();
      if (view.getImageTabs().getTabCount() == 0) {
        forceExit();
      }
    }
  }

  private File file; //hacky, i know
  private File promptFile(final boolean isOpen) {
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
