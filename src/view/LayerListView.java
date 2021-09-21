package view;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;

import controller.actions.layer.*;
import static utils.Constants.BLEND_MODES;
import controller.composites.*;
import static utils.DrawingHelper.EMPTY_PAINTER;
import model.Layer;
import model.Document;

public class LayerListView extends JPanel
        implements ChangeListener, ActionListener, DocumentListener {
    private View view;
    private JPanel layerList;
    private GridBagConstraints layerConstraint = new GridBagConstraints();
    private ButtonGroup layerGroup = new ButtonGroup();
    private JTextField nameField;
    private JComboBox<BlendComposite> blendComboBox;
    private JSlider opacitySlider;
    private JSpinner opacitySpinner;
    private JPanel layerActionsPanel;
    private LayerAction[] layerActions = new LayerAction[] {new AddEmptyLayerAction(this),
            new RemoveLayerAction(this), new DuplicateLayerAction(this), new MergeLayerAction(this),
            new MoveUpLayerAction(this), new MoveDownLayerAction(this)};

    public LayerListView(final View view) {
        // setup and setting variables
        this.view = view;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(null, "Layers"));
        layerConstraint.gridwidth = GridBagConstraints.REMAINDER;
        layerConstraint.weightx = 1;
        layerConstraint.fill = GridBagConstraints.HORIZONTAL;
        Border padding = BorderFactory.createEmptyBorder(2, 5, 5, 5);

        // PROPERTIES
        // defining the name property
        JPanel nameProperty = new JPanel();
        nameProperty.setLayout(new BoxLayout(nameProperty, BoxLayout.X_AXIS));
        nameProperty.add(new JLabel("Name:   "));
        nameField = new JTextField();
        nameField.getDocument().addDocumentListener(this);
        nameProperty.add(nameField);
        nameProperty.setBorder(padding);

        // defining the blend property
        JPanel blendProperty = new JPanel();
        blendProperty.setLayout(new BoxLayout(blendProperty, BoxLayout.X_AXIS));
        blendProperty.add(new JLabel("Blend Mode:   "));
        blendComboBox = new JComboBox<BlendComposite>(BLEND_MODES) {
            @Override
            public void setSelectedItem(Object item) {
                if (item == null)
                    return;
                if (item == "")
                    getModel().setSelectedItem("");
                super.setSelectedItem(item);
            }
        };
        blendComboBox.setRenderer(new ComboBoxRenderer<BlendComposite>());
        blendComboBox.addActionListener(this);
        blendProperty.add(blendComboBox);
        blendProperty.setBorder(padding);

        // defining the opacity property
        JPanel opacityProperty = new JPanel();
        opacityProperty.setLayout(new BoxLayout(opacityProperty, BoxLayout.X_AXIS));
        opacityProperty.add(new JLabel("Opacity:"));
        opacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 255);
        opacitySlider.setSnapToTicks(true);
        opacitySlider.addChangeListener(this);

        opacitySpinner = new JSpinner(new SpinnerNumberModel(100d, 0d, 100d, 1d));
        Dimension opacitySpinnerSize = new Dimension(85, 22);
        opacitySpinner.setMinimumSize(opacitySpinnerSize);
        opacitySpinner.setMaximumSize(opacitySpinnerSize);
        opacitySpinner.setPreferredSize(opacitySpinnerSize);
        opacitySpinner.setEditor(new JSpinner.NumberEditor(opacitySpinner, "##0.##'%'"));
        opacitySpinner.addChangeListener(this);
        opacityProperty.add(opacitySlider);
        opacityProperty.add(opacitySpinner);
        opacityProperty.setBorder(padding);

        // layer properties container
        JPanel layerProperties = new JPanel();
        layerProperties.setLayout(new BoxLayout(layerProperties, BoxLayout.Y_AXIS));
        layerProperties.add(nameProperty);
        layerProperties.add(blendProperty);
        layerProperties.add(opacityProperty);
        add(layerProperties, BorderLayout.NORTH);

        // LAYER LIST
        layerList = new JPanel(new GridBagLayout());
        layerList.setBackground(view.CONTENT_BACKGROUND);
        JScrollPane scrollPane = new JScrollPane(layerList);
        add(scrollPane);

        // actions
        layerActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        add(layerActionsPanel, BorderLayout.SOUTH);
        Dimension buttonSize = new Dimension(24, 24);
        layerActionsPanel.setPreferredSize(new Dimension(0, buttonSize.height + 2));
        for (LayerAction action : layerActions) {
            JButton button = new JButton(action);
            button.setPreferredSize(buttonSize);
            button.setMinimumSize(buttonSize);
            button.setMaximumSize(buttonSize);
            button.setBorderPainted(false);
            button.setBackground(null);
            button.setOpaque(false);
            button.setFocusable(false);
            UIDefaults def = new UIDefaults();
            def.put("Button[Disabled].backgroundPainter", EMPTY_PAINTER);
            def.put("Button[Enabled].backgroundPainter", EMPTY_PAINTER);
            button.putClientProperty("Nimbus.Overrides", def);
            layerActionsPanel.add(button);
        }

        updateProperties();
    }

    public void update() {
        layerList.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.weighty = 1;
        JPanel filler = new JPanel();
        filler.setPreferredSize(new Dimension(0, 0));
        layerList.add(filler, gbc);

        if (view.hasSelectedDocument()) {
            for (Layer layer : view.getSelectedDocument().getLayers()) {
                LayerView layerView = new LayerView(this, layer);
                layerGroup.add(layerView);
                layerList.add(layerView, layerConstraint, 0);
                if (layer == view.getSelectedDocumentView().getSelectedLayer()) {
                    layerView.setSelected(true);
                }
            }
        }
        updateProperties();
        updateLayerActionAbility();

        validate();
        repaint();
    }

    public void updateProperties() {
        boolean enabled = view.hasSelectedDocument();
        nameField.setEnabled(enabled);
        if (enabled) {
            Layer layer = view.getSelectedDocumentView().getSelectedLayer();
            nameField.setText(layer.getName());
            blendComboBox.setSelectedIndex(layer.getBlendIndex());
            opacitySpinner.setValue(layer.getOpacity() * 100d);

            enabled = enabled && layer.isVisible();
        } else {
            nameField.setText("");
            blendComboBox.setSelectedItem("");
            opacitySpinner.setValue(0);
            opacitySlider.setValue(0);
        }
        opacitySlider.setEnabled(enabled);
        opacitySpinner.setEnabled(enabled);
        blendComboBox.setEnabled(enabled);
    }

    public void updateLayerActionAbility() {
        for (Component c : layerActionsPanel.getComponents()) {
            c.setEnabled(((JButton) c).getAction().isEnabled());
        }
    }

    @Override // listener for opacity
    public void stateChanged(ChangeEvent e) {
        if (!view.hasSelectedDocument())
            return;
        Object source = e.getSource();
        float opacity;
        if (source instanceof JSlider) {
            opacity = (float) opacitySlider.getValue() / 255f;
            opacitySpinner.removeChangeListener(this);
            opacitySpinner.setValue(opacity * 100d);
            opacitySpinner.addChangeListener(this);
        } else if (source instanceof JSpinner) {
            opacity = (float) ((double) opacitySpinner.getValue() / 100d);
            opacitySlider.removeChangeListener(this);
            opacitySlider.setValue((int) (opacity * 255));
            opacitySlider.addChangeListener(this);
        } else {
            return;
        }
        DocumentView docView = view.getSelectedDocumentView();
        docView.getSelectedLayer().setOpacity(opacity);
        docView.getDocument().updateFlattenedCache();
        docView.getCanvas().repaint();
    }

    @Override // listener for blend mode
    public void actionPerformed(ActionEvent e) {
        if (!view.hasSelectedDocument())
            return;
        @SuppressWarnings("unchecked")
        JComboBox<BlendComposite> combo = (JComboBox<BlendComposite>) e.getSource();
        DocumentView docView = view.getSelectedDocumentView();
        Document doc = docView.getDocument();
        docView.getSelectedLayer().setBlendComposite(combo.getSelectedIndex());
        doc.updateFlattenedCache();
        docView.getCanvas().repaint();
    }

    @Override // listener for name
    public void changedUpdate(DocumentEvent e) {
        pushLayerName();
    }

    @Override // listener for name
    public void insertUpdate(DocumentEvent e) {
        pushLayerName();
    }

    @Override // listener for name
    public void removeUpdate(DocumentEvent e) {
        pushLayerName();
    }

    private void pushLayerName() {
        if (!view.hasSelectedDocument())
            return;
        Layer layer = view.getSelectedDocumentView().getSelectedLayer();
        layer.setName(nameField.getText());
        getSelectedLayerView().updateName();
    }

    public View getView() {
        return view;
    }

    public LayerView getLayerView(Layer layer) {
        for (Component c : layerList.getComponents()) {
            if (!(c instanceof LayerView))
                continue;
            LayerView layerView = (LayerView) c;
            if (layerView.getLinkedLayer() == layer)
                return layerView;
        }
        return null;
    }

    public LayerView getSelectedLayerView() {
        return getLayerView(view.getSelectedDocumentView().getSelectedLayer());
    }
}
