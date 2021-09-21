package controller.filters;

import java.awt.event.*;
import view.View;

public class PosterizeFilter extends FilterAction {
    public PosterizeFilter(View view) {
        super(view, "Posterize");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        for (int i = 0; i < pixels.length; i++) {
            int r = red(pixels[i]);
            int g = green(pixels[i]);
            int b = blue(pixels[i]);
            pixels[i] = getColor(alpha(pixels[i]), r - r % 64, g - g % 64, b - b % 64);
        }
        update();
    }
}

