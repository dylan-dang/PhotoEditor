package view;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.Arrays;

import javax.swing.*;

import model.Document;
import model.SnapShotManager;
import controller.actions.menubar.*;
import static utils.DrawingHelper.EMPTY_PAINTER;
import static utils.DrawingHelper.drawChecker;
import controller.actions.tool.*;
import model.Layer;

public class DocumentView extends JPanel {
    public final float[] ZOOM_TABLE = {2, 3, 4, 5, 6, 7, 8, 9, 10, 12.5f, 17, 20, 25, 33.33f, 50,
            66.67f, 100, 150, 200, 300, 400, 500, 600, 800, 1000, 1200, 1400, 1600, 2000, 2400,
            3200, 4000, 4800, 5600, 6400};
    private final int INFOBAR_HEIGHT = 24;
    private final Timer selectionAnimator = new Timer(25, new ActionListener() {
        private int cycle = 0;
        public void actionPerformed(ActionEvent evt) {
            canvas.selectionBlackDash = new BasicStroke(1, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[] {3}, 5 - cycle);
            canvas.selectionWhiteDash = new BasicStroke(1, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, new float[] {3}, 5 - ((cycle + 3) % 6));
            Rectangle bounds = getScaledSelection().getBounds();
            bounds.grow(1, 1); // confirm entire selection is repainted
            canvas.repaint(bounds);
            cycle = (cycle + 1) % 6;
        }
    });
    private float scale = 1;

    private final View view;
    private final Document document;
    private int selectedLayerIndex;
    private Shape selection;

    private JPanel infoBar;
    private final JLabel toolTipLabel;
    private final JLabel imageSizeLabel;
    private final JLabel positionLabel;
    private JButton fitToWindow, zoomOut, zoomIn;
    private JSpinner zoomSpinner;
    private JSlider zoomSlider;
    private JViewport viewport;
    private JPanel canvasWrapper;
    private Canvas canvas;

    public SnapShotManager snapShotManager;

    DocumentView(Document document, View view) {
        this.view = view;
        this.document = document;
        this.snapShotManager = new SnapShotManager(this);
        this.selectedLayerIndex = 0;
        setLayout(new BorderLayout());

        toolTipLabel = new JLabel(
                (ImageIcon) view.getToolBar().getSelectedTool().getValue(Action.SMALL_ICON));
        toolTipLabel.setMinimumSize(new Dimension(0, INFOBAR_HEIGHT));

        Dimension labelSize = new Dimension(128, INFOBAR_HEIGHT);
        imageSizeLabel = new JLabel();
        updateImageSizeLabel();
        imageSizeLabel.setIcon(infoBarIcon("imageSize.png"));
        imageSizeLabel.setPreferredSize(labelSize);
        imageSizeLabel.setMaximumSize(labelSize);

        positionLabel = new JLabel("0, 0px");
        positionLabel.setIcon(infoBarIcon("position.png"));
        positionLabel.setPreferredSize(labelSize);
        positionLabel.setMaximumSize(labelSize);

        setupInfoBar();
        setupViewport();
    }

    private ImageIcon infoBarIcon(String string) {
        return new ImageIcon(String.format("assets/infoBar/%s", string));
    }

    private void setupInfoBar() {
        setupZoomControl();
        infoBar = new JPanel();
        infoBar.setLayout(new BoxLayout(infoBar, BoxLayout.X_AXIS));
        infoBar.setPreferredSize(new Dimension(0, INFOBAR_HEIGHT));
        infoBar.add(toolTipLabel);
        infoBar.add(Box.createGlue());
        addInfoSeparator();
        infoBar.add(imageSizeLabel);
        addInfoSeparator();
        infoBar.add(positionLabel);
        addInfoSeparator();
        infoBar.add(zoomSpinner);
        infoBar.add(new JLabel("%"));
        infoBar.add(fitToWindow);
        infoBar.add(zoomOut);
        infoBar.add(zoomSlider);
        infoBar.add(zoomIn);
        add(infoBar, BorderLayout.SOUTH);
    }

    private void addInfoSeparator() {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        Dimension size = new Dimension(separator.getPreferredSize().width,
                separator.getMaximumSize().height);
        separator.setMaximumSize(size);
        infoBar.add(separator);
        infoBar.add(Box.createRigidArea(new Dimension(5, INFOBAR_HEIGHT)));
    }

    private void setupZoomControl() {
        JButton[] buttons = {fitToWindow = new JButton(new ZoomToWindowAction(view)),
                zoomOut = new JButton(new ZoomOutAction(view)),
                zoomIn = new JButton(new ZoomInAction(view))};
        for (JButton button : buttons) {
            Dimension buttonSize = new Dimension(24, INFOBAR_HEIGHT);
            button.setText(null);
            button.setEnabled(true);
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
        }

        fitToWindow.setIcon(infoBarIcon("fitToWindow.png"));
        zoomOut.setIcon(infoBarIcon("zoomOut.png"));
        zoomIn.setIcon(infoBarIcon("zoomIn.png"));
        setupZoomSpinner();
        setupZoomSlider();
    }

    private void setupZoomSpinner() {
        zoomSpinner = new JSpinner(new SpinnerNumberModel(100d, 0.1d, 6400d, 1d));
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(zoomSpinner, "##0.##");
        zoomSpinner.setFocusable(false);
        zoomSpinner.setMaximumSize(editor.getTextField().getPreferredSize());
        zoomSpinner.setEditor(editor);
        zoomSpinner.setBorder(null);
        zoomSpinner.setBackground(null);
        zoomSpinner.addChangeListener(e -> {
            float value = ((Number) zoomSpinner.getValue()).floatValue();
            setScale(value / 100);
        });
    }

    private void setupZoomSlider() {
        int defaultIndex = Arrays.binarySearch(ZOOM_TABLE, 100);
        zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, ZOOM_TABLE.length - 1, defaultIndex);
        Dimension sliderSize = new Dimension(110, INFOBAR_HEIGHT);
        zoomSlider.setPreferredSize(sliderSize);
        zoomSlider.setMinimumSize(sliderSize);
        zoomSlider.setMaximumSize(sliderSize);
        zoomSlider.setSnapToTicks(true);
        zoomSlider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (source.getValueIsAdjusting())
                setScale(ZOOM_TABLE[source.getValue()] / 100);
        });
    }

    private void setupViewport() {
        canvasWrapper = new JPanel(new GridBagLayout());
        canvasWrapper.add(canvas = new Canvas());
        canvasWrapper.setBackground(view.CONTENT_BACKGROUND);

        CanvasMouseListener canvasMouseListener = new CanvasMouseListener();
        canvas.addMouseWheelListener(canvasMouseListener);
        canvas.addMouseMotionListener(canvasMouseListener);
        canvas.addMouseListener(canvasMouseListener);
        canvasWrapper.addMouseMotionListener(canvasMouseListener);
        canvasWrapper.addMouseWheelListener(canvasMouseListener);
        canvasWrapper.addMouseListener(canvasMouseListener);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);

        viewport = scrollPane.getViewport();
        viewport.add(canvasWrapper);
        viewport.addChangeListener(e -> viewport.repaint());
    }

    public class Canvas extends JPanel {
        final Stroke singleWhiteDash = new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[] {1}, 0);
        final Stroke singleBlackDash = new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL, 0, new float[] {1}, 1);
        public Stroke selectionWhiteDash = new BasicStroke();
        public Stroke selectionBlackDash = new BasicStroke();

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            Rectangle viewRect = viewport.getViewRect();
            int deltax = viewRect.x % 16;
            int deltay = viewRect.y % 16;
            drawChecker(g2, viewRect.x - deltax, viewRect.y - deltay, viewRect.width + deltax - 1,
                    viewRect.height + deltay - 1, 8);
            AffineTransform preScale = g2.getTransform();
            g2.scale(scale, scale);
            g2.drawImage(document.flattened(), 0, 0, null);
            g2.setTransform(preScale);

            if (view.isPixelGridEnabled() && (viewRect.height + viewRect.width) / scale < 50) {
                drawPixelGrid(g2, viewRect);
            }
            // hacky way of showing selection on bottom and right edges consistently
            g2.setColor(view.CONTENT_BACKGROUND);
            Dimension size = getPreferredSize();
            g2.drawLine(--size.width, 0, size.width, --size.height);
            g2.drawLine(0, size.height, size.width, size.height);

            if (hasSelection()) {
                drawDoubleDashed(g2, getScaledSelection(), selectionWhiteDash, selectionBlackDash);
            }
            g2.dispose();
        }

        private void drawPixelGrid(Graphics2D g2, Rectangle viewRect) {
            int startingRow = (int) (viewRect.y / scale);
            for (int row = startingRow + 1; row < startingRow + viewRect.height / scale
                    + 1; row++) {
                int y = (int) (scale * row);
                drawDoubleDashed(g2,
                        new Line2D.Float(viewRect.x, y, viewRect.x + viewRect.width, y));
            }
            int startingCol = (int) (viewRect.x / scale);
            for (int col = startingCol + 1; col < startingCol + viewRect.width / scale
                    + 1; col++) {
                int x = (int) (scale * col);
                drawDoubleDashed(g2,
                        new Line2D.Float(x, viewRect.y, x, viewRect.y + viewRect.height));
            }
        }

        private void drawDoubleDashed(Graphics2D g2, Shape shape, Stroke white, Stroke black) {
            g2.setColor(Color.white);
            g2.setStroke(white);
            g2.draw(shape);
            g2.setColor(Color.black);
            g2.setStroke(black);
            g2.draw(shape);
        }

        private void drawDoubleDashed(Graphics2D g2, Shape shape) {
            drawDoubleDashed(g2, shape, singleWhiteDash, singleBlackDash);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(Math.round(document.getWidth() * scale + 1),
                    Math.round(document.getHeight() * scale + 1));
        }

        public boolean largerThan(Dimension container) {
            return getPreferredSize().width > container.getWidth()
                    || getPreferredSize().height > container.getHeight();
        }
    }

    private class CanvasMouseListener
            implements MouseMotionListener, MouseWheelListener, MouseListener {
        Point2D pos = new Point2D.Double(0, 0);
        ToolAction tool;
        DragGesture dragState;
        // Point origin;

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isControlDown()) {
                setScale(
                        (float) Math.min(
                                Math.max(scale * Math.pow(1.1, -e.getWheelRotation()), .01), 64),
                        e.getPoint());
                return;
            }
            Rectangle view = viewport.getViewRect();
            int delta = e.getWheelRotation() * 50;
            if (e.isShiftDown()) {
                view.x += delta;
            } else {
                view.y += delta;
            }

            ((JPanel) viewport.getView()).scrollRectToVisible(view);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            updatePos(e);
            updatePostionLabel();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            updatePos(e);
            dragState.dragTo(pos);
            tool.dragging();
            canvas.repaint();
            updatePostionLabel();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            // origin = e.getPoint();
            updatePos(e);
            if (!dragState.isDragging()) {
                dragState.start(pos, e.getButton());
            } else {
                dragState.pressButton(e.getButton());
            }
            tool.dragStarted();
            tool.dragging();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            updatePos(e);
            dragState.releaseButton(e.getButton());
            if (dragState.getButtons().isEmpty()) {
                dragState.stop(pos);
            }
            tool.dragEnded();
        }

        void updatePos(MouseEvent e) {
            Point mouse = e.getPoint();
            tool = view.getToolBar().getSelectedTool();
            toolTipLabel.setText(tool.getToolTip());
            dragState = tool.getDragState();
            JComponent source = (JComponent) e.getSource();
            if (source.getLayout() instanceof GridBagLayout) { // wrapper
                Component canvas = source.getComponent(0);
                mouse.translate(-(source.getWidth() - canvas.getWidth()) / 2,
                        -(source.getHeight() - canvas.getHeight()) / 2);
            }
            pos.setLocation(mouse.x / scale, mouse.y / scale);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            updatePos(e);
            tool.click(pos, e.getButton());
        }

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseExited(MouseEvent e) {}

        private void updatePostionLabel() {
            positionLabel.setText(String.format("%d, %dpx", (int) pos.getX(), (int) pos.getY()));
        }
    }

    public Shape getSelection() {
        if (selection == null)
            return new Rectangle2D.Double(0, 0, document.getWidth(), document.getHeight());
        return selection;
    }

    public Shape getScaledSelection() {
        if (selection == null)
            return new Rectangle2D.Double(0, 0, document.getWidth() * scale,
                    document.getHeight() * scale);
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        return transform.createTransformedShape(selection);
    }

    public void setSelection(Shape selection) {
        this.selection = selection;
        canvas.repaint();
        if (hasSelection()) {
            selectionAnimator.start();
        } else {
            selectionAnimator.stop();
        }
    }

    public boolean hasSelection() {
        return selection != null;
    }

    public void setCanvasBackground(Color c) {
        canvasWrapper.setBackground(c);
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale, Point2D pos) {
        if (pos == null) {
            setScale(scale);
            return;
        }

        float deltaScale = scale / this.scale;

        this.scale = scale;
        // if canvas is smaller than viewport, no need to translate the view position
        if (canvas.largerThan(viewport.getExtentSize())) {
            Point viewPos = viewport.getViewPosition();
            viewport.setViewPosition(
                    new Point((int) Math.round(viewPos.x + pos.getX() * deltaScale - pos.getX()),
                            (int) Math.round(viewPos.y + pos.getY() * deltaScale - pos.getY())));
        }

        int lastTick = 0;
        while (lastTick < ZOOM_TABLE.length && scale >= ZOOM_TABLE[lastTick] / 100) {
            lastTick++;
        }
        if (!zoomSlider.getValueIsAdjusting())
            zoomSlider.setValue(lastTick);
        zoomSpinner.setValue(scale * 100d);

        canvas.revalidate();
        viewport.repaint();
    }

    public void setScale(float scale) {
        // default to center of viewrect
        Rectangle viewRect = viewport.getViewRect();
        setScale(scale,
                new Point(viewRect.x + viewRect.width / 2, viewRect.y + viewRect.height / 2));
    }

    public Layer getSelectedLayer() {
        return document.getLayers().get(selectedLayerIndex);
    }

    public int getSelectedLayerIndex() {
        return selectedLayerIndex;
    }

    public void setSelectedLayer(Layer layer) {
        selectedLayerIndex = document.getLayers().indexOf(layer);
        view.getLayerListView().update();
    }

    public void setSelectedLayerIndex(int index) {
        selectedLayerIndex = index;
        view.getLayerListView().update();
    }


    public Document getDocument() {
        return document;
    }

    public JViewport getViewport() {
        return viewport;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setToolTipText(String text) {
        toolTipLabel.setText(text);
    }

    public void setToolTipIcon(Icon icon) {
        toolTipLabel.setIcon(icon);
    }

    public void updateImageSizeLabel() {
        imageSizeLabel
                .setText(String.format("%d x %dpx", document.getWidth(), document.getHeight()));
    }

    public SnapShotManager getSnapShotManager() {
        return snapShotManager;
    }

    public void save() {
        snapShotManager.save();
    }
}
