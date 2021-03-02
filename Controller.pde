import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class Controller {
  private ArrayList<Document> docs;
  private View view;
  final JFXPanel jfxPanel = new JFXPanel();
  Controller controller = this;

  Controller(View view) {
    this.view = view;
    this.docs = new ArrayList<Document>();
    addAllMenuBarItems();
  }

  void addDocument(Document doc) {
    docs.add(doc);
    view.addDocumentView(doc);
  }

  public void addAllMenuBarItems() {
    JMenu fileMenu = view.getMenu("File");
    JMenu editMenu = view.getMenu("Edit");
    JMenu viewMenu = view.getMenu("View");
    JMenu imageMenu = view.getMenu("Image");
    JMenu layerMenu = view.getMenu("Layer");
    JMenu filterMenu = view.getMenu("Filter");
    fileMenu.add(new NewFileAction());
    fileMenu.add(new OpenFileAction());
    fileMenu.addSeparator();
    fileMenu.add(new SaveAction());
    fileMenu.add(new SaveAsAction());
    fileMenu.addSeparator();
    fileMenu.add(new CloseFileAction());
    fileMenu.add(new CloseAllAction());
    fileMenu.add(new CloseOtherAction());
    fileMenu.addSeparator();
    fileMenu.add(new PrintAction());
    fileMenu.addSeparator();
    fileMenu.add(new ExitAction());

  }



  //Menu bar actions
  private abstract class MenuBarAction extends AbstractAction {
    public MenuBarAction(String name, String accelerator) {
      putValue(NAME, name);
      putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator));
    }
  }

  //File Menu Actions
  public class NewFileAction extends MenuBarAction {
    public NewFileAction() {
      super("New...", "ctrl N");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      controller.addDocument(new Document(800, 600));
    }
  }

  public class OpenFileAction extends MenuBarAction {
    File file;

    public OpenFileAction() {
      super("Open...", "ctrl O");
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

}
