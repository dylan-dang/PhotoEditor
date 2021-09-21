package view;

import java.awt.*;
import javax.swing.*;
import controller.actions.tool.ToolAction;

class ComboBoxRenderer<T> extends JLabel implements ListCellRenderer<T> {
    private JSeparator separator;

    public ComboBoxRenderer() {
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        separator = new JSeparator(JSeparator.HORIZONTAL);
    }

    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index,
            boolean isSelected, boolean cellHasFocus) {
        if (value == null)
            return separator;
        setFont(list.getFont());
        if (value instanceof ToolAction) {
            ToolAction tool = (ToolAction) value;
            setText(tool.getName());
            setIcon(tool.getIcon());
        } else {
            setText(value.toString());
        }
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}
