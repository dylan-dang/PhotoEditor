public class LayerListView extends JPanel {
	private View view;
	private JPanel mainList;
  JButton addLayerButton;
  GridBagConstraints layerConstraint = new GridBagConstraints();
  ButtonGroup layerGroup = new ButtonGroup();

	public LayerListView(View view) {
		this.view = view;
		setLayout(new BorderLayout());
    layerConstraint.gridwidth = GridBagConstraints.REMAINDER;
    layerConstraint.weightx = 1;
    layerConstraint.fill = GridBagConstraints.HORIZONTAL;

		mainList = new JPanel(new GridBagLayout());

		add(new JScrollPane(mainList));

		addLayerButton = new JButton("Add");
    addLayerButton.setEnabled(false);
    add(addLayerButton, BorderLayout.SOUTH);
		addLayerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
        LayerView layerView = new LayerView(null);
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
        LayerView layerView = new LayerView(layer);
        layerGroup.add(layerView);
        mainList.add(layerView, layerConstraint, 0);
      }
    } else {
      addLayerButton.setEnabled(false);
    }
    validate();
    repaint();
  }
}

public class LayerView extends JToggleButton {
	Layer layer;
	LayerView(Layer layer) {
		this.layer = layer;
		setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
		add(new JLabel("bruh"), new ImageIcon(""));
	}
}
