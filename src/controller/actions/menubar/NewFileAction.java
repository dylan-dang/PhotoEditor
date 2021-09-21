package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import view.View;
import model.Document;

public class NewFileAction extends MenuBarAction {
    public NewFileAction(View view) {
        super(view, "New...", "ctrl N");
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
        labels.add(new JLabel("Width:", SwingConstants.RIGHT));
        labels.add(new JLabel("Height:", SwingConstants.RIGHT));

        JPanel inputs = new JPanel(new GridLayout(0, 1, 2, 2));
        JSpinner width, height;
        inputs.add(width = new JSpinner(new SpinnerNumberModel(800, 1, Short.MAX_VALUE, 1)));
        inputs.add(height = new JSpinner(new SpinnerNumberModel(600, 1, Short.MAX_VALUE, 1)));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(labels, BorderLayout.WEST);
        panel.add(inputs, BorderLayout.CENTER);

        int confirmation = JOptionPane.showConfirmDialog(view.getFrame(), panel, "New Image",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirmation == JOptionPane.CANCEL_OPTION)
            return;

        int index = view.getImageTabs().getSelectedIndex() + 1;
        view.insertDocument(new Document((int) width.getValue(), (int) height.getValue()), index);
        view.getImageTabs().setSelectedIndex(index);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

