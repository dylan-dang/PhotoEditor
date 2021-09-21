package controller.actions.menubar;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import view.View;
import model.Document;

public class ResizeAction extends MenuBarAction {
    public ResizeAction(View view) {
        super(view, "Resize...", "ctrl R");
    }

    class Item {
        String name;
        Object value;

        Item(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String toString() {
            return name;
        }

        Object getValue() {
            return value;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Document doc = view.getSelectedDocument();

        JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
        labels.add(new JLabel("Resampling:", SwingConstants.RIGHT));
        labels.add(new JLabel("Width:", SwingConstants.RIGHT));
        labels.add(new JLabel("Height:", SwingConstants.RIGHT));
        labels.add(new JLabel("Percentage:", SwingConstants.RIGHT));

        JPanel inputs = new JPanel(new GridLayout(0, 1, 2, 2));
        JComboBox<Item> resampling;
        JSpinner width, height, percentage;
        inputs.add(resampling = new JComboBox<Item>(
                new Item[] {new Item("Bicubic", RenderingHints.VALUE_INTERPOLATION_BICUBIC),
                        new Item("Bilinear", RenderingHints.VALUE_INTERPOLATION_BILINEAR),
                        new Item("Nearest Neighbor",
                                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)}));
        inputs.add(width =
                new JSpinner(new SpinnerNumberModel(doc.getWidth(), 1, Short.MAX_VALUE, 1)));
        inputs.add(height =
                new JSpinner(new SpinnerNumberModel(doc.getHeight(), 1, Short.MAX_VALUE, 1)));
        inputs.add(percentage =
                new JSpinner(new SpinnerNumberModel(100d, 1d, (double) Short.MAX_VALUE, 1d)));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(labels, BorderLayout.WEST);
        panel.add(inputs, BorderLayout.CENTER);

        int confirmation = JOptionPane.showConfirmDialog(view.getFrame(), panel, "Resize",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (confirmation == JOptionPane.CANCEL_OPTION)
            return;

        createSnapshot();
        doc.resize(
                (int) Math.min(
                        Math.max(Math.round(
                                (Double) percentage.getValue() / 100d * (int) width.getValue()), 1),
                        Short.MAX_VALUE),
                (int) Math.min(Math.max(
                        Math.round((Double) percentage.getValue() / 100d * (int) height.getValue()),
                        1), Short.MAX_VALUE),
                ((Item) resampling.getSelectedItem()).getValue());
        updateDocView();

    }
}

