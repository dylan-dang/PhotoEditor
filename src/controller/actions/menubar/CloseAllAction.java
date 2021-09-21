package controller.actions.menubar;

import java.awt.event.*;
import javax.swing.*;
import view.View;

public class CloseAllAction extends MenuBarAction {
    public CloseAllAction(View view) {
        super(view, "Close All", "ctrl alt W");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final JTabbedPane imageTabs = view.getImageTabs();
        final int tabCount = imageTabs.getTabCount();
        for (int i = tabCount - 1; i >= 0; i--) {
            imageTabs.setSelectedIndex(i);
            CloseFileAction closeFileAction = new CloseFileAction(view, i);
            closeFileAction.execute();
            if (!closeFileAction.getSuccess()) {
                break;
            }
        }
    }
}

