package controller.actions.tool;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.*;
import javax.swing.*;
import view.View;
import static utils.Constants.sampling;

public class EyeDropAction extends ToolAction {
    public EyeDropAction(View view) {
        super("Eyedropper Tool", "eyedrop.png", view);
        options.add(new JLabel("Sampling: "));
        options.add(sampling);
    }

    public void dragging() {
        super.initVars();
        if (!imageRect.contains(current))
            return;

        BufferedImage samplingImage =
                (String) sampling.getSelectedItem() == "Image" ? doc.flattened()
                        : selectedLayer.getImage();
        Color c = new Color(samplingImage.getRGB((int) current.getX(), (int) current.getY()), true);

        if (buttons.contains(MouseEvent.BUTTON1))
            selector.setPrimary(c);
        if (buttons.contains(MouseEvent.BUTTON3))
            selector.setSecondary(c);
    }
}

