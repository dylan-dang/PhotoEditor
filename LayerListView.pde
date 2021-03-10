public class LayerListView extends JPanel {
  private View view;
  private JPanel mainList;
  private JButton addLayerButton;
  private GridBagConstraints layerConstraint = new GridBagConstraints();
  private ButtonGroup layerGroup = new ButtonGroup();
  private JComboBox blendComboBox;
  private JSlider opacitySlider;
  private JSpinner opacitySpinner;
  public final BlendComposite[] blendModes = new BlendComposite[] {
    new NormalComposite(),
    null,
    new DarkenComposite(),
    new MultiplyComposite(),
    new ColorBurnComposite(),
    new AdditiveComposite(),
    null,
    new LightenComposite(),
    new ScreenComposite(),
    new ColorDodgeComposite(),
    new SubtractiveComposite(),
    null,
    new OverlayComposite(),
    new SoftLightComposite(),
    new HardLightComposite(),
    new VividLightComposite(),
    new LinearLightComposite(),
    new PinLightComposite(),
    new HardMixComposite(),
    null,
    new DifferenceComposite(),
    new ExclusionComposite(),
    null,
    new XorComposite(),
    new AndComposite(),
    new OrComposite()
  };

  public LayerListView(final View view) {
    this.view = view;
    setLayout(new BorderLayout());
    layerConstraint.gridwidth = GridBagConstraints.REMAINDER;
    layerConstraint.weightx = 1;
    layerConstraint.fill = GridBagConstraints.HORIZONTAL;

    mainList = new JPanel(new GridBagLayout());

		JPanel blendProperty = new JPanel();
		blendProperty.setLayout(new BoxLayout(blendProperty, BoxLayout.X_AXIS));
		blendProperty.add(new JLabel("Blend Mode:   "));
    blendComboBox = new JComboBox(blendModes) {
      @Override
      public void setSelectedItem(Object item) {
        if (item == null) return;
        super.setSelectedItem(item);
      }
			@Override
			public boolean isEnabled() {
				return view.hasSelectedDocument();
			}
    };
    blendComboBox.setRenderer(new ComboBoxRenderer());
    blendComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JComboBox combo = (JComboBox) e.getSource();
				DocumentView docView = view.getSelectedDocumentView();
				Document doc = docView.getDocument();
				docView.getSelectedLayer().setBlendComposite((BlendComposite)combo.getSelectedItem()); //TODO get selected layer
				doc.updateFlattenedView();
				docView.repaint();
      }
    });
    blendProperty.add(blendComboBox);
    blendProperty.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel opacityProperty = new JPanel();
    opacityProperty.setLayout(new BoxLayout(opacityProperty, BoxLayout.X_AXIS));
    opacityProperty.add(new JLabel("Opacity:"));
    opacitySlider = new JSlider(JSlider.HORIZONTAL, 0, 255, 255);
    opacitySlider.setSnapToTicks(true);

    opacitySpinner = new JSpinner(new SpinnerNumberModel(100d, 0d, 100.1d, 100d/255d));
    //((JSpinner.DefaultEditor) opacitySpinner.getEditor()).getTextField().setColumns(1);
    opacitySpinner.setMinimumSize(new Dimension(72, 22));
    opacitySpinner.setMaximumSize(new Dimension(72, 22));
    opacitySpinner.setPreferredSize(new Dimension(72, 22));
    opacitySpinner.setEditor(new JSpinner.NumberEditor(opacitySpinner, "##0.##'%'"));
    opacityProperty.add(opacitySlider);
    opacityProperty.add(opacitySpinner);

    opacityProperty.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

    JPanel layerProperties = new JPanel(new BorderLayout());
    layerProperties.add(blendProperty, BorderLayout.NORTH);
    layerProperties.add(opacityProperty, BorderLayout.SOUTH);
    add(layerProperties, BorderLayout.NORTH);

    JScrollPane scrollPane = new JScrollPane(mainList);
    setBorder(BorderFactory.createTitledBorder(null, "Layers"));

    //scrollPane.setBorder(null);
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
        mainList.add(layerView, layerConstraint, 0);

        validate();
        repaint();
      }
    });
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(200, 200);
  }
  public View getView() {
    return view;
  }
  public void update() {
    mainList.removeAll();
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1;
    gbc.weighty = 1;
    mainList.add(new JPanel(), gbc);
    if (view.getSelectedDocument() != null) {
      addLayerButton.setEnabled(true);
      for(Layer layer: view.getSelectedDocument().getLayers()) {
        LayerView layerView = new LayerView(this, layer);
        layerGroup.add(layerView);
        mainList.add(layerView, layerConstraint, 0);
        if (layer == view.getSelectedDocumentView().getSelectedLayer()) {
          layerView.setSelected(true);
        }
      }
    } else {
      addLayerButton.setEnabled(false);
    }
    validate();
    repaint();
  }
  public void updateProperties() {
    BlendComposite blend = view.getSelectedDocumentView().getSelectedLayer().getBlendComposite();
    if (blend == null) {
      blendComboBox.setSelectedItem(blendModes[0]);
      return;
    };
    blendComboBox.setSelectedItem(blend);
  }
}

public class LayerView extends JToggleButton implements ActionListener {
  LayerListView parent;
  Layer layer;
  Image thumbnail;
  private final int MAXLENGTH = 54;
  LayerView(LayerListView parent, Layer layer) {
    this.parent = parent;
    this.layer = layer;
    updateThumbnail();
    setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY),
      BorderFactory.createEmptyBorder(6, 6, 6, 6)
    ));
    JLabel layerLabel = new JLabel("bruh");
    layerLabel.setIcon(new ImageIcon(thumbnail));
    add(layerLabel);
    addActionListener(this);
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
    BufferedImage thumb = new BufferedImage(width + 2, height + 2, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = thumb.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    g.setColor(Color.white);
    g.fillRect(1, 1, width, height);
    g.setColor(DrawHelper.CHECKER);
    DrawHelper.drawChecker(g, 1, 1, width, height, 5);
    g.setColor(Color.gray);
    g.drawRect(0, 0, width + 1, height + 1);
    g.drawImage(img, 1, 1, width, height, null);
    g.dispose();
    thumbnail = thumb;
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    parent.getView().getSelectedDocumentView().setSelectedLayer(layer);
    parent.updateProperties();
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
