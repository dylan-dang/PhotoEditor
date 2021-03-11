public class LayerListView extends JPanel implements ChangeListener, ActionListener {
  private View view;
  private JPanel layerList;
  private JButton addLayerButton;
  private GridBagConstraints layerConstraint = new GridBagConstraints();
  private ButtonGroup layerGroup = new ButtonGroup();
  private JComboBox blendComboBox;
  private JSlider opacitySlider;
  private JSpinner opacitySpinner;

  public LayerListView(final View view) {
    //setup and setting variables
    this.view = view;
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createTitledBorder(null, "Layers"));
    layerConstraint.gridwidth = GridBagConstraints.REMAINDER;
    layerConstraint.weightx = 1;
    layerConstraint.fill = GridBagConstraints.HORIZONTAL;
    Border padding = BorderFactory.createEmptyBorder(2, 5, 5, 5);

    //PROPERTIES
    //defining the name property
    JPanel nameProperty = new JPanel();
    nameProperty.setLayout(new BoxLayout(nameProperty, BoxLayout.X_AXIS));
    nameProperty.add(new JLabel("Name:   "));
    nameProperty.add(new JTextField());
    nameProperty.setBorder(padding);

    //defining the blend property
		JPanel blendProperty = new JPanel();
		blendProperty.setLayout(new BoxLayout(blendProperty, BoxLayout.X_AXIS));
		blendProperty.add(new JLabel("Blend Mode:   "));
    blendComboBox = new JComboBox(BLEND_MODES) {
      @Override
      public void setSelectedItem(Object item) {
        if (item == null) return;
        super.setSelectedItem(item);
      }
    };
    blendComboBox.setRenderer(new ComboBoxRenderer());
    blendComboBox.addActionListener(this);
    blendProperty.add(blendComboBox);
    blendProperty.setBorder(padding);

    //defining the opacity property
    JPanel opacityProperty = new JPanel();
    opacityProperty.setLayout(new BoxLayout(opacityProperty, BoxLayout.X_AXIS));
    opacityProperty.add(new JLabel("Opacity:"));
    opacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 255);
    opacitySlider.setSnapToTicks(true);
    opacitySlider.addChangeListener(this);

    opacitySpinner = new JSpinner(new SpinnerNumberModel(100d, 0d, 100d, 1d));
    opacitySpinner.setMinimumSize(new Dimension(72, 22));
    opacitySpinner.setMaximumSize(new Dimension(72, 22));
    opacitySpinner.setPreferredSize(new Dimension(72, 22));
    opacitySpinner.setEditor(new JSpinner.NumberEditor(opacitySpinner, "##0.##'%'"));
    opacitySpinner.addChangeListener(this);
    opacityProperty.add(opacitySlider);
    opacityProperty.add(opacitySpinner);
    opacityProperty.setBorder(padding);

    //layer properties container
    JPanel layerProperties = new JPanel(new BorderLayout());
    layerProperties.add(nameProperty, BorderLayout.NORTH);
    layerProperties.add(blendProperty, BorderLayout.CENTER);
    layerProperties.add(opacityProperty, BorderLayout.SOUTH);
    add(layerProperties, BorderLayout.NORTH);

    //LAYER LIST
    layerList = new JPanel(new GridBagLayout());
    layerList.setBackground(new Color(0x333333)); //probably shouldn't hardcode this but oh well
    JScrollPane scrollPane = new JScrollPane(layerList);
    add(scrollPane);

    addLayerButton = new JButton("Add");
    addLayerButton.setEnabled(false);
    add(addLayerButton, BorderLayout.SOUTH);
    final LayerListView thisLayerListView = this;
    addLayerButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        Document doc = view.getSelectedDocument();
        Layer layer = doc.addLayer();
        LayerView layerView = new LayerView(thisLayerListView, layer);
        layerGroup.add(layerView);
        layerList.add(layerView, layerConstraint, 0);

        validate();
        repaint();
      }
    });
    updateProperties();
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(200, 200);
  }

  public View getView() {
    return view;
  }

  public void update() {
    layerList.removeAll();

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1;
    gbc.weighty = 1;
    JPanel filler = new JPanel();
    filler.setPreferredSize(new Dimension(0,0));
    layerList.add(filler, gbc);

    if (view.getSelectedDocument() != null) {
      addLayerButton.setEnabled(true);
      for(Layer layer: view.getSelectedDocument().getLayers()) {
        LayerView layerView = new LayerView(this, layer);
        layerGroup.add(layerView);
        layerList.add(layerView, layerConstraint, 0);
        if (layer == view.getSelectedDocumentView().getSelectedLayer()) {
          layerView.setSelected(true);
        }
      }
    } else {
      addLayerButton.setEnabled(false);
    }
    updateProperties();
    validate();
    repaint();
  }

  public void updateProperties() {
    boolean enabled = view.hasSelectedDocument();
    if (enabled) {
      Layer layer = view.getSelectedDocumentView().getSelectedLayer();
      blendComboBox.setSelectedIndex(layer.getBlendIndex());

      opacitySpinner.setValue(layer.getOpacity() * 100d);

      enabled = enabled && layer.isVisible();
    }
    opacitySlider.setEnabled(enabled);
    opacitySpinner.setEnabled(enabled);
    blendComboBox.setEnabled(enabled);
  }

  public void updateThumbnail(Layer layer) {
    for (Component c: layerList.getComponents()) {
      if (!(c instanceof LayerView)) continue;
      LayerView layerView = (LayerView) c;
      if (layerView.getLinkedLayer() == layer) {
        layerView.updateThumbnail();
      }
    }
  }

  public void updateThumbnails() {
    for (Component c: layerList.getComponents()) {
      if (!(c instanceof LayerView)) continue;
      ((LayerView) c).updateThumbnail();
    }
  }

  @Override
  public void stateChanged(ChangeEvent e) {
    Object source = e.getSource();
    float opacity;
    if (source instanceof JSlider) {
      opacity = (float) opacitySlider.getValue() / 255f;
      opacitySpinner.removeChangeListener(this);
      opacitySpinner.setValue(opacity * 100d);
      opacitySpinner.addChangeListener(this);
    } else if (source instanceof JSpinner) {
      opacity = (float) ((double)opacitySpinner.getValue() / 100d);
      opacitySlider.removeChangeListener(this);
      opacitySlider.setValue((int)(opacity * 255));
      opacitySlider.addChangeListener(this);
    } else {
      return;
    }
    DocumentView docView = view.getSelectedDocumentView();
    docView.getSelectedLayer().setOpacity(opacity);
    docView.getDocument().updateFlattenedView();
    docView.getCanvas().repaint();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    JComboBox combo = (JComboBox) e.getSource();
    DocumentView docView = view.getSelectedDocumentView();
    Document doc = docView.getDocument();
    docView.getSelectedLayer().setBlendComposite(combo.getSelectedIndex());
    doc.updateFlattenedView();
    docView.getCanvas().repaint();
  }

  public JComboBox getBlendComboBox() {
    return blendComboBox;
  }

  public JSlider getOpacitySlider() {
    return opacitySlider;
  }

  public JSpinner getOpacitySpinner() {
    return opacitySpinner;
  }
}

public class LayerView extends JToggleButton implements ActionListener, ItemListener {
  LayerListView parent;
  Layer layer;
  JLabel layerLabel = new JLabel();
  private final ImageIcon VISIBLE = new ImageIcon(sketchPath("resources/layers/visible.png"));
  private final ImageIcon INVISIBLE = new ImageIcon(sketchPath("resources/layers/invisible.png"));
  JToggleButton visibilityButton;
  private final int MAXLENGTH = 54;

  LayerView(LayerListView parent, Layer layer) {
    this.parent = parent;
    this.layer = layer;
    setLayout(new BorderLayout());

    setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
      BorderFactory.createEmptyBorder(6, 6, 6, 6)
    ));
    addActionListener(this);

    layerLabel.setText(layer.getName());
    layerLabel.setPreferredSize(new Dimension(64, 32));
    updateThumbnail();
    add(layerLabel);

    visibilityButton = new JToggleButton(VISIBLE);
    visibilityButton.addItemListener(this);
    visibilityButton.setPreferredSize(new Dimension(24, 24));
    visibilityButton.setContentAreaFilled(false);
    visibilityButton.setBorderPainted(false);

    JPanel wrapper = new JPanel();
    wrapper.setLayout(new GridBagLayout());
    wrapper.setBackground(null);
    wrapper.setOpaque(false);
    wrapper.add(visibilityButton);
    wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
    add(wrapper, BorderLayout.EAST);
  }
  private void updateThumbnail() {
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
    BufferedImage thumbnail = new BufferedImage(width + 2, height + 2, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = thumbnail.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    g.setColor(Color.white);
    g.fillRect(1, 1, width, height);
    g.setColor(DrawHelper.CHECKER);
    DrawHelper.drawChecker(g, 1, 1, width, height, 5);
    g.setColor(Color.gray);
    g.drawRect(0, 0, width + 1, height + 1);
    g.drawImage(img, 1, 1, width, height, null);
    g.dispose();
    layerLabel.setIcon(new ImageIcon(thumbnail));
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    parent.getView().getSelectedDocumentView().setSelectedLayer(layer);
    parent.updateProperties();
  }
  @Override
  public void itemStateChanged(ItemEvent e) {
    if (visibilityButton.isSelected()) {
      visibilityButton.setIcon(VISIBLE);
      layer.setVisible(true);
    } else {
      visibilityButton.setIcon(INVISIBLE);
      layer.setVisible(false);
    }
    parent.updateProperties();
    DocumentView docView = parent.getView().getSelectedDocumentView();
    docView.getDocument().updateFlattenedView();
    docView.getCanvas().repaint();
  }
  public Layer getLinkedLayer() {
    return layer;
  }
}

class ComboBoxRenderer extends JLabel implements ListCellRenderer {
  JSeparator separator;

  public ComboBoxRenderer() {
    setOpaque(true);
    setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    separator = new JSeparator(JSeparator.HORIZONTAL);
  }

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    if (value == null) return separator;
    String str = value.toString();
    if (isSelected) {
      setBackground(list.getSelectionBackground());
      setForeground(list.getSelectionForeground());
    } else {
      setBackground(list.getBackground());
      setForeground(list.getForeground());
    }
    setFont(list.getFont());
    setText(str);
    return this;
  }
}
