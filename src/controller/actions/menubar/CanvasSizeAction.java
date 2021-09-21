package controller.actions.menubar;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import view.View;
import model.Document;

public class CanvasSizeAction extends MenuBarAction {
    public CanvasSizeAction(View view) {
        super(view, "Canvas size...", "ctrl shift R");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Document doc = view.getSelectedDocument();

        JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
        labels.add(new JLabel("Width:", SwingConstants.RIGHT));
        labels.add(new JLabel("Height:", SwingConstants.RIGHT));
        labels.add(new JLabel("Anchor:", SwingConstants.RIGHT));

        JPanel inputs = new JPanel(new GridLayout(0, 1, 2, 2));
        JSpinner width, height;
        JComboBox<String> anchor = new JComboBox<String>(
                new String[] {"Top Left", "Top Center", "Top Right", "Center Left", "Center Center",
                        "Center Right", "Bottom Left", "Bottom Center", "Bottom Right"});
        inputs.add(width =
                new JSpinner(new SpinnerNumberModel(doc.getWidth(), 1, Short.MAX_VALUE, 1)));
        inputs.add(height =
                new JSpinner(new SpinnerNumberModel(doc.getHeight(), 1, Short.MAX_VALUE, 1)));
        inputs.add(anchor);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(labels, BorderLayout.WEST);
        panel.add(inputs, BorderLayout.CENTER);

        int confirmation = JOptionPane.showConfirmDialog(view.getFrame(), panel, "Resize",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirmation == JOptionPane.CANCEL_OPTION)
            return;

        createSnapshot();
        doc.changeCanvasSize((int) width.getValue(), (int) height.getValue(),
                (String) anchor.getSelectedItem());
        updateDocView();
    }
}

