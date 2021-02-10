public class Controller {
  private Document model;
  private View view;

  Controller(Document model, View view) {
    this.model = model;
    this.view = view;
    SwingUtilities.invokeLater(new MenuBarListener());
  }

  private class MenuBarListener implements ActionListener, Runnable {
    @Override
    public void run() {
      for (int i = 0; i < view.menubar.fileMenu.length; i++) {
        view.menubar.fileMenu[i].addActionListener(this);
      }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
      switch (e.getActionCommand()) {
        case "exit": exit(); break;
        default: println(e.getActionCommand());
      }
    }
  }

}
