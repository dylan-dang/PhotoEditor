package controller.actions.menubar;

import view.View;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public abstract class FileMenuBarAction extends MenuBarAction {
    protected JFileChooser jfc = new JFileChooser();

    public FileMenuBarAction(View view, String name, Object accelerator) {
        super(view, name, accelerator);
        jfc.setFileFilter(new FileNameExtensionFilter("All Image Types",
                "png", "jpg", "jpeg", "jpe", "jif", "jfif", "exif", "bmp", "dib", "rle", "wbmp", "gif"));
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("JPEG (*.jpg; *.jpeg; *.jpe; *.jif; *.jfif; *.exif)", "jpg", "jpeg", "jpe", "jif", "jfif", "exif"));
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("BMP (*.bmp; *.dib; *.rle)", "bmp", "dib", "rle"));
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("WBMP (*.wbmp)", "wbmp"));
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("GIF (*.gif)", "gif"));
    }

    protected File openFile() {
        int option = jfc.showOpenDialog(null);
        return getSelectedFile(option);
    }

    protected File saveFile() {
        int option = jfc.showSaveDialog(null);
        return getSelectedFile(option);
    }

    private File getSelectedFile(int option) {
        if (option != JFileChooser.APPROVE_OPTION) return null;
        return jfc.getSelectedFile();
    }
}
