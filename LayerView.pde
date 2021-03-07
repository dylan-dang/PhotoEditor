public class LayerView extends JPanel {

    private JPanel mainList;

    public LayerView() {
        setLayout(new BorderLayout());

        mainList = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.weighty = 1;
        mainList.add(new JPanel(), gbc);

        add(new JScrollPane(mainList));

        JButton add = new JButton("Add");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPanel panel = new JPanel();
                panel.add(new JLabel("Hello"));
                panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                gbc.weightx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                mainList.add(panel, gbc, 0);

                validate();
                repaint();
            }
        });

        add(add, BorderLayout.SOUTH);

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 200);
    }
}
