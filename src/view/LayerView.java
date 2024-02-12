package view;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import model.Layer;

import static utils.DrawingHelper.CHECKER;
import static utils.DrawingHelper.drawChecker;

public class LayerView extends JToggleButton implements ActionListener, ItemListener {
    LayerListView parent;
    Layer layer;
    JLabel layerLabel = new JLabel();
    private final ImageIcon VISIBLE = new ImageIcon("assets/layers/visible.png");
    private final ImageIcon INVISIBLE = new ImageIcon("assets/layers/invisible.png");
    JToggleButton visibilityButton;
    private final int MAXLENGTH = 38;

    LayerView(LayerListView parent, Layer layer) {
        this.parent = parent;
        this.layer = layer;
        setLayout(new BorderLayout());

        /*
         * setBorder(BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder(1, 1, 1, 1,
         * Color.GRAY), BorderFactory.createEmptyBorder(6, 6, 6, 6) ));
         */

        this.setFocusable(false);
        this.setRolloverEnabled(false);
        addActionListener(this);

        layerLabel.setPreferredSize(new Dimension(64, 40));
        update();
        add(layerLabel);

        visibilityButton = new JToggleButton(INVISIBLE, layer.isVisible());
        visibilityButton.setSelectedIcon(VISIBLE);
        visibilityButton.setRolloverEnabled(false);
        visibilityButton.addItemListener(this);
        visibilityButton.setPreferredSize(new Dimension(24, 24));
        visibilityButton.setContentAreaFilled(false);
        visibilityButton.setBorderPainted(false);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridBagLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(visibilityButton);
        add(wrapper, BorderLayout.EAST);
    }

    public void update() {
        updateName();
        updateThumbnail();
    }

    public void updateThumbnail() {
        final BufferedImage img = layer.getImage();
        int width, height;
        final int imgHeight = img.getHeight();
        final int imgWidth = img.getWidth();
        if (imgHeight > imgWidth) {
            width = imgWidth * MAXLENGTH / imgHeight;
            height = MAXLENGTH;
        } else {
            width = MAXLENGTH;
            height = imgHeight * MAXLENGTH / imgWidth;
        }
        BufferedImage thumbnail =
                new BufferedImage(width + 2, height + 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumbnail.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setColor(Color.white);
        g.fillRect(1, 1, width, height);
        g.setColor(CHECKER);
        drawChecker(g, 1, 1, width, height, 5);
        g.setColor(Color.gray);
        g.drawRect(0, 0, width + 1, height + 1);
        g.drawImage(img, 1, 1, width, height, null);
        g.dispose();
        layerLabel.setIcon(new ImageIcon(thumbnail));
    }

    public void updateName() {
        layerLabel.setText(layer.getName());
    }

    @Override // layer selected
    public void actionPerformed(ActionEvent e) {
        parent.getView().getSelectedDocumentView().setSelectedLayer(layer);
        parent.updateProperties();
        parent.updateLayerActionAbility();
    }

    @Override // visibilityButton button state listener
    public void itemStateChanged(ItemEvent e) {
        layer.setVisible(visibilityButton.isSelected());
        parent.updateProperties();
        DocumentView docView = parent.getView().getSelectedDocumentView();
        docView.getDocument().updateFlattenedCache();
        docView.getCanvas().repaint();
    }

    public Layer getLinkedLayer() {
        return layer;
    }
}
