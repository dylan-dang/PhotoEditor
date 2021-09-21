package controller.filters;

import java.awt.event.*;
import view.View;

public class FantasyFilter extends FilterAction {
    public FantasyFilter(View view) {
        super(view, "Fantasy Filter");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = getColor(alpha(pixels[i]), blue(pixels[i]) + 83, green(pixels[i]) + 80,
                    red(pixels[i]) + 80);
        }
        update();
    }
}

