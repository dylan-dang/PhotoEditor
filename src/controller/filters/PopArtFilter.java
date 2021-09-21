package controller.filters;

import java.awt.event.*;
import view.View;

public class PopArtFilter extends FilterAction {
    public PopArtFilter(View view) {
        super(view, "Pop Art Filter");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        int tempPixel1 = 0;
        for (int i = 0; i < pixels.length; i++) {
            if (i % 3 == 0) {
                int tempPixel2 = pixels[i];
                pixels[i] = getColor(alpha(tempPixel1), red(tempPixel1), green(tempPixel1),
                        blue(tempPixel1));
                tempPixel1 = tempPixel2;
            }
            i += 5;
        }
        update();
    }
}

