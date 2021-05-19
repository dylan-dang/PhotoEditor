public Hashtable<String, String> login(JFrame frame) {
    Hashtable<String, String> logininformation = new Hashtable<String, String>();


    JPanel labels = new JPanel(new GridLayout(0, 1, 2, 2));
    labels.add(new JLabel("E-Mail", SwingConstants.RIGHT));
    labels.add(new JLabel("Password", SwingConstants.RIGHT));

    JPanel inputs = new JPanel(new GridLayout(0, 1, 2, 2));
    JSpinner width, height;
    inputs.add(width = new JSpinner());
    inputs.add(height = new JSpinner());

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(labels, BorderLayout.WEST);
    panel.add(inputs, BorderLayout.CENTER);

    JOptionPane.showMessageDialog(frame, panel, "Resize", JOptionPane.PLAIN_MESSAGE);

    // logininformation.put("user", username.getText());
    // logininformation.put("pass", new String(password.getPassword()));
    return logininformation;
}