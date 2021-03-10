import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;


//Super Menu bar action
private abstract class MenuBarAction extends AbstractAction {
  protected View view;

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
  protected File promptFile(final boolean isOpen) {
    final FileChooser fileChooser = new FileChooser();
    if (isOpen) fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Image Types", "*.png", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif", "*.bmp", ".dib", "*.wbmp", "*.gif"));
    fileChooser.getExtensionFilters().addAll(
       new ExtensionFilter("PNG", "*.png"),
       new ExtensionFilter("JPEG", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif"),
       new ExtensionFilter("BMP", "*.bmp", ".dib"),
       new ExtensionFilter("WBMP", "*.wbmp"),
       new ExtensionFilter("GIF", "*.gif"));
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
  @Override
  public boolean isEnabled() {
    return view.hasSelectedDocument();
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
    int index = view.getImageTabs().getSelectedIndex() + 1;
    view.addDocument(new Document(800, 600), index);
    view.getImageTabs().setSelectedIndex(index);
  }
  @Override
  public boolean isEnabled() {
    return true;
  }
}

public class OpenFileAction extends MenuBarAction {

  public OpenFileAction(View view) {
    super(view, "Open...", "ctrl O");
    setEnabled(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    File file = promptFile(true);
    if (file == null) return;
    int index = view.getImageTabs().getSelectedIndex() + 1;
    view.addDocument(new Document(file), index);
    view.getImageTabs().setSelectedIndex(index);
  }
  @Override
  public boolean isEnabled() {
    return true;
  }
}

public class SaveAction extends MenuBarAction {
  private Document document;
  public SaveAction(View view, Document document) {
    super(view, "Save", "ctrl S");
    this.document = document;
  }
  public SaveAction(View view) {
    this(view, null);
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    Document doc = document == null ? view.getSelectedDocument() : document;
    if (!doc.isLinked()) {
      new SaveAsAction(view, doc).execute();
      return;
    }
    File file = doc.getLinkedFile();
    try {
      ImageIO.write(doc.getFlattenedView(), "png", file);
    } catch (IOException ioex) {
      //TODO
    }
    doc.setSaved(true);
  }
}

public class SaveAsAction extends MenuBarAction {
  private Document document;
  public SaveAsAction(View view, Document document) {
    super(view, "Save As...", "ctrl shift S");
    this.document = document;
  }
  public SaveAsAction(View view) {
    this(view, null);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Document doc = document == null ? view.getSelectedDocument() : document;
    File file = promptFile(false);
    if (file == null) return;
    doc.setLinkedFile(file);
    new SaveAction(view, doc).execute();
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
        String.format("Save changes to \"%s\" before closing?", docView.getDocument().getName()),
        "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[SAVE]);
      switch(response) {
        case CANCEL: return;
        case SAVE: new SaveAction(view, docView.getDocument()).execute();
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
    final JTabbedPane imageTabs = view.getImageTabs();
    final int tabCount = imageTabs.getTabCount();
    for(int i = tabCount - 1; i >= 0; i--) {
      imageTabs.setSelectedIndex(i);
      CloseFileAction closeFileAction = new CloseFileAction(view, i);
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
    final JTabbedPane imageTabs = view.getImageTabs();
    final int tabCount = imageTabs.getTabCount();
    final int selected = imageTabs.getSelectedIndex();
    for(int i = tabCount - 1; i >= 0; i--) {
      if (i == selected) continue;
      imageTabs.setSelectedIndex(i);
      CloseFileAction closeFileAction = new CloseFileAction(view, i);
      closeFileAction.execute();
      if (!closeFileAction.getSuccess()) {
        break;
      }
    }
  }
  @Override
  public boolean isEnabled() {
    if (view.getImageTabs() == null) return false;
    return view.getImageTabs().getTabCount() > 1;
  }
}

public class PrintAction extends MenuBarAction {
  Document doc;
  public PrintAction(View view) {
    this(view, null);
  }
  public PrintAction(View view, Document doc) {
    super(view, "Print...", "ctrl P");
    this.doc = doc;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (doc == null) doc = view.getSelectedDocument();
    if (doc == null) return;
    PrinterJob printJob = PrinterJob.getPrinterJob();
    printJob.setPrintable(new PrintableImage(printJob, doc.getFlattenedView()));
    if (printJob.printDialog()) {
      try {
        printJob.print();
      } catch (PrinterException prt) {
        prt.printStackTrace();
      }
    }
  }
  public class PrintableImage implements Printable {
    private double x, y, width;
    private int orientation;
    private BufferedImage image;

    public PrintableImage(PrinterJob printJob, BufferedImage image) {
      PageFormat pageFormat = printJob.defaultPage();
      this.x = pageFormat.getImageableX();
      this.y = pageFormat.getImageableY();
      this.width = pageFormat.getImageableWidth();
      this.orientation = pageFormat.getOrientation();
      this.image = image;
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
      if (pageIndex == 0) {
        int pWidth = 0;
        int pHeight = 0;
        if (orientation == PageFormat.PORTRAIT) {
          pWidth = (int) Math.min(width, (double) image.getWidth());
          pHeight = pWidth * image.getHeight() / image.getWidth();
        } else {
          pHeight = (int) Math.min(width, (double) image.getHeight());
          pWidth = pHeight * image.getWidth() / image.getHeight();
        }
        g.drawImage(image, (int) x, (int) y, pWidth, pHeight, null);
        return PAGE_EXISTS;
      } else {
        return NO_SUCH_PAGE;
      }
    }
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
  @Override
  public boolean isEnabled() {
    return true;
  }
}
