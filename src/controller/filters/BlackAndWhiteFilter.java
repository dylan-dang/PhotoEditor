package controller.filters;

import java.awt.event.*;
import view.View;

public class BlackAndWhiteFilter extends FilterAction {
    public BlackAndWhiteFilter(View view) {
        super(view, "Black and White");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        for (int i = 0; i < pixels.length; i++) {
            int average = red(pixels[i]) / 3 + green(pixels[i]) / 3 + blue(pixels[i]) / 3;
            pixels[i] = getColor(alpha(pixels[i]), average, average, average);
        }
        update();
    }
}

