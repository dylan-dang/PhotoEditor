package controller.filters;

import java.awt.event.*;
import view.View;

public class ChromaticAbberationFilter extends FilterAction {
    public ChromaticAbberationFilter(View view) {
        super(view, "Chromatic Abberation");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        int[] original = pixels.clone();
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] =
                    getColor(alpha(pixels[i]), red(original[Math.floorMod(i - 5, pixels.length)]),
                            green(original[i]), blue(original[(i + 5) % pixels.length]));
        }
        update();
    }
}
