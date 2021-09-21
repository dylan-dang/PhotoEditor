package view;

import java.util.HashMap;
import java.util.Vector;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Container;
import java.awt.Insets;

public class MultiBorderLayout extends BorderLayout {
    private final String[] SIDE_NAMES = {"North", "South", "West", "East", "Center"};
    private HashMap<String, Vector<Component>> sides = new HashMap<String, Vector<Component>>();

    public MultiBorderLayout(int hgap, int vgap) {
        super(hgap, vgap);
        for (String sideName : SIDE_NAMES)
            sides.put(sideName, new Vector<Component>());
    }

    public MultiBorderLayout() {
        this(0, 0);
    }

    public void addLayoutComponent(String name, Component component) {
        synchronized (component.getTreeLock()) {
            name = name == null ? "Center" : name;
            try {
                sides.get(name).add(component);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "cannot add to layout: unknown constraint: " + name);
            }
        }
    }

    public void removeLayoutComponent(Component component) {
        synchronized (component.getTreeLock()) {
            for (String sideName : SIDE_NAMES)
                sides.get(sideName).remove(component);
        }
    }

    private Dimension getLayoutSize(Container target, boolean isPreferred) {
        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);
            Component c;

            for (String sideName : SIDE_NAMES) {
                for (int i = 0; i < sides.get(sideName).size(); i++) {
                    c = (Component) sides.get(sideName).get(i);
                    if (!c.isVisible())
                        continue;
                    Dimension d = isPreferred ? c.getPreferredSize() : c.getMinimumSize();

                    switch (sideName) {
                        case "North":
                        case "South":
                            dim.width = Math.max(d.width, dim.width);
                            dim.height += d.height + this.getVgap();
                            break;
                        case "East":
                        case "West":
                            dim.width += d.width + this.getHgap();
                            dim.height = Math.max(d.height, dim.height);
                            break;
                        case "Center":
                            dim.width += d.width;
                            dim.height = Math.max(d.height, dim.height);
                            break;
                    }
                }
            }

            Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;

            return dim;
        }
    }

    public Dimension minimumLayoutSize(Container target) {
        return getLayoutSize(target, false);
    }

    public Dimension prefferedLayoutSize(Container target) {
        return getLayoutSize(target, true);
    }

    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int top = insets.top;
            int bottom = target.getHeight() - insets.bottom;
            int left = insets.left;
            int right = target.getWidth() - insets.right;
            Component c;

            for (String sideName : SIDE_NAMES) {
                for (int i = 0; i < sides.get(sideName).size(); i++) {
                    c = (Component) sides.get(sideName).get(i);
                    if (!c.isVisible())
                        continue;
                    Dimension d = c.getPreferredSize();

                    switch (sideName) {
                        case "North":
                            c.setSize(right - left, d.height);
                            c.setBounds(left, top, right - left, c.getHeight());
                            top += d.height;
                            break;
                        case "South":
                            c.setSize(right - left, d.height);
                            c.setBounds(left, bottom - d.height, right - left, c.getHeight());
                            bottom -= d.height;
                            break;
                        case "East":
                            c.setSize(d.width, bottom - top);
                            c.setBounds(right - d.width, top, c.getWidth(), bottom - top);
                            right -= d.width;
                            break;
                        case "West":
                            c.setSize(d.width, bottom - top);
                            c.setBounds(left, top, c.getWidth(), bottom - top);
                            left += d.width;
                            break;
                        case "Center":
                            c.setBounds(left, top, right - left, bottom - top);
                            break;
                    }
                }
            }
        }
    }
}
