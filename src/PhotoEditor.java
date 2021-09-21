import javax.swing.JFrame;
import view.View;

public class PhotoEditor extends JFrame {
    public PhotoEditor() {
        super();
        setTitle("Photo Editor");
        setSize(800, 600);
        new View(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        new PhotoEditor();
    }
}
