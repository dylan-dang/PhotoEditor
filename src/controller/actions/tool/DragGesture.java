package controller.actions.tool;

import java.util.*;
import java.awt.geom.*;

public class DragGesture {
    private Point2D start = new Point2D.Double(), last = new Point2D.Double(),
            current = new Point2D.Double(), end = new Point2D.Double();
    private boolean dragging = false;
    private Set<Integer> buttons = new HashSet<Integer>();

    public void start(Point2D start, int button) {
        this.start.setLocation(start);
        this.last.setLocation(start);
        this.current.setLocation(start);
        buttons.add(button);
    }

    public void stop(Point2D end) {
        this.end.setLocation(end);
        dragging = false;
    }

    public boolean isDragging() {
        return dragging;
    }

    public Point2D getStart() {
        return start;
    }

    public Point2D getEnd() {
        if (dragging)
            return null;
        return end;
    }

    public void dragTo(Point2D pos) {
        dragging = true;
        last.setLocation(current);
        current.setLocation(pos);
    }

    public Point2D getCurrent() {
        return current;
    }

    public Point2D getLast() {
        return last;
    }

    public Set<Integer> getButtons() {
        return buttons;
    }

    public void pressButton(int button) {
        buttons.add(button);
    }

    public void releaseButton(int button) {
        buttons.remove(button);
    }
}
