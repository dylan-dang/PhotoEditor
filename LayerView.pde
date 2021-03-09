public class LayerListView extends JPanel {
	private View view;
	private JPanel mainList;
  JButton addLayerButton;
  GridBagConstraints layerConstraint = new GridBagConstraints();
  ButtonGroup layerGroup = new ButtonGroup();

	public LayerListView(final View view) {
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
				Document doc = view.getSelectedDocument();
				Layer layer = doc.addLayer();
        LayerView layerView = new LayerView(layer);
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
	Image thumbnail;
	private final int MAXLENGTH = 54;
	LayerView(Layer layer) {
		this.layer = layer;
		updateThumbnail();
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(Color.GRAY),
			BorderFactory.createEmptyBorder(6, 6, 6, 6)
		));
		JLabel layerLabel = new JLabel("bruh");
		layerLabel.setIcon(new ImageIcon(thumbnail));
		add(layerLabel);
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

}
