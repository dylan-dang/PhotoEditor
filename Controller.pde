import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class Controller {
  private ArrayList<Document> docs;
  private View view;
  Controller controller = this;
  DocumentView docview;

  Controller(View view) {
    this.view = view;
    this.docs = new ArrayList<Document>();
    new MenuBarController().addAllMenuBarItems();
  }

  void addDocument(Document doc) {
    docs.add(doc);
    docview = view.addDocumentView(doc);
  }

  private class MenuBarController {
    //needed to embed native file chooser into swing
    final JFXPanel jfxPanel = new JFXPanel();

    public void addAllMenuBarItems() {
      addFileMenuItems();
      addEditMenuItems();
      addFilterMenuItems();
    }

    private void addFileMenuItems() {
      JMenu fileMenu = view.getMenuBar().getFileMenu();
      fileMenu.add(new NewFileAction(controller));
      fileMenu.add(new OpenFileAction(controller, view));
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

    private void addEditMenuItems() {
      JMenu editMenu = view.getMenuBar().getEditMenu();
    }

    private void addViewMenuItems() {
      JMenu viewMenu = view.getMenuBar().getViewMenu();
    }

    private void addImageMenuItems() {
      JMenu imageMenu = view.getMenuBar().getImageMenu();
    }

    private void addLayerMenuItems() {
      JMenu layerMenu = view.getMenuBar().getLayerMenu();
    }

    private void addFilterMenuItems() {
      JMenu filterMenu = view.getMenuBar().getFilterMenu();
      filterMenu.add(new TestFilter());
    }

    //EditMenu Actions

    public class TestFilter extends AbstractAction {
      public TestFilter() {
        super("test filter");
      }
      @Override
      void actionPerformed(ActionEvent e) {
        if (docs.isEmpty()) return;
        byte[] pixels = ((DataBufferByte) docs.get(0).getEditView().getRaster().getDataBuffer()).getData();
        for (int i = 0; i < pixels.length; i += 2) {
          pixels[i] = pixels[pixels.length - i - 1];
        }
        println("done");
        docview.updateImage();
      }
    }
  }

}
