import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class Controller {
  private View view;
  final JFXPanel jfxPanel = new JFXPanel();
  
  Controller(View view) {
    this.view = view;
    final Controller controller = this;
    view.getFrame().addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        new ExitAction(controller).execute();
      }
    });
    addAllMenuBarItems();
  }

  public void addAllMenuBarItems() {
    addMenuActions(view.getMenu("File"), new MenuBarAction[] {
      new NewFileAction(this),
      new OpenFileAction(this),
      null,
      new SaveAction(this),
      new SaveAsAction(this),
      null,
      new CloseFileAction(this),
      new CloseAllAction(this),
      new CloseOtherAction(this),
      null,
      new PrintAction(this),
      null,
      new ExitAction(this)
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

  public View getView() {
    return view;
  }
}
