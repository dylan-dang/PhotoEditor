package controller.filters;

import java.awt.event.*;
import view.View;

public class SepiaFilter extends FilterAction {
    public SepiaFilter(View view) {
        super(view, "Sepia");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        for (int i = 0; i < pixels.length; i++) {
            int r = red(pixels[i]);
            int g = green(pixels[i]);
            int b = blue(pixels[i]);
            pixels[i] = getColor(alpha(pixels[i]), (int) (0.393 * r + 0.769 * g + 0.189 * b),
                    (int) (0.349 * r + 0.686 * g + 0.168 * b),
                    (int) (0.272 * r + 0.534 * g + 0.131 * b));
        }
        update();
    }
}

