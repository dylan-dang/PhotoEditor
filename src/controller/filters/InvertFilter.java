package controller.filters;

import java.awt.event.*;
import view.View;

public class InvertFilter extends FilterAction {
    public InvertFilter(View view) {
        super(view, "Invert");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = getColor(alpha(pixels[i]), 255 - red(pixels[i]), 255 - green(pixels[i]),
                    255 - blue(pixels[i]));
        }
        update();
    }
}

