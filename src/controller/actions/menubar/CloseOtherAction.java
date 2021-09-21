package controller.actions.menubar;

import java.awt.event.*;
import javax.swing.*;
import view.View;

public class CloseOtherAction extends MenuBarAction {
    public CloseOtherAction(View view) {
        super(view, "Close Others", "ctrl alt P");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final JTabbedPane imageTabs = view.getImageTabs();
        final int tabCount = imageTabs.getTabCount();
        final int selected = imageTabs.getSelectedIndex();
        for (int i = tabCount - 1; i >= 0; i--) {
            if (i == selected)
                continue;
            imageTabs.setSelectedIndex(i);
            CloseFileAction closeFileAction = new CloseFileAction(view, i);
            closeFileAction.execute();
            if (!closeFileAction.getSuccess()) {
                break;
            }
        }
    }

    @Override
    public boolean isEnabled() {
        if (view.getImageTabs() == null)
            return false;
        return view.getImageTabs().getTabCount() > 1;
    }
}

