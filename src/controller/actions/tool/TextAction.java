package controller.actions.tool;

import view.View;

public class TextAction extends ToolAction {
    TextAction(View view) {
        super("Text Tool", "text.png", view);
    }

    public void dragging() {
        if (!selectedLayer.isVisible())
            return;
    }
}

