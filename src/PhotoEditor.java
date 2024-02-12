import javax.swing.JFrame;
import view.View;
import com.formdev.flatlaf.FlatDarkLaf;

public class PhotoEditor extends JFrame {
    public PhotoEditor() {
        super();
        FlatDarkLaf.setup();
        setTitle("Photo Editor");
        setSize(800, 600);
        new View(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        new PhotoEditor();
    }
}
