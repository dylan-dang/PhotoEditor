package controller.actions.menubar;

import java.awt.event.*;
import view.View;

public class ExitAction extends MenuBarAction {
    public ExitAction(View view) {
        super(view, "Exit", "ctrl Q");
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new CloseAllAction(view).execute();
        if (view.getImageTabs().getTabCount() == 0) {
            // forceExit();
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
// Edit Menu Actions
