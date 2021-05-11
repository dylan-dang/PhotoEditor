import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import java.util.concurrent.CountDownLatch; 
import java.awt.*; 
import java.awt.event.*; 
import java.awt.event.KeyEvent; 
import java.awt.event.MouseEvent; 
import java.awt.image.*; 
import java.awt.geom.*; 
import javax.swing.*; 
import javax.swing.border.Border; 
import javax.swing.plaf.basic.BasicSpinnerUI; 
import javax.swing.plaf.basic.BasicToolBarUI; 
import javax.swing.event.*; 
import javax.imageio.*; 
import javafx.application.Platform; 
import javafx.embed.swing.JFXPanel; 
import javafx.stage.FileChooser; 
import javafx.stage.FileChooser.ExtensionFilter; 
import java.awt.print.PageFormat; 
import java.awt.print.Printable; 
import java.awt.print.PrinterException; 
import java.awt.print.PrinterJob; 
import java.awt.*; 
import java.awt.datatransfer.*; 
import java.awt.dnd.*; 
import java.awt.geom.*; 
import java.awt.image.*; 
import javax.swing.*; 
import java.awt.event.MouseAdapter; 
import java.awt.event.MouseEvent; 
import javax.swing.plaf.basic.BasicToolBarUI; 
import javax.swing.event.AncestorEvent; 
import javax.swing.event.AncestorListener; 

import com.formdev.flatlaf.*; 
import com.formdev.flatlaf.ui.*; 
import com.formdev.flatlaf.util.*; 
import com.formdev.flatlaf.json.*; 
import com.formdev.flatlaf.icons.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class PhotoEditor extends PApplet {




























public void setup() {
    //styling (isn't necessary to run the program but sets up the theme)
    com.formdev.flatlaf.FlatDarkLaf.install();
    
    
    System.setProperty("apple.laf.useScreenMenuBar", "true"); //menubar to to top, for macs
    JFrame frame = (JFrame)((processing.awt.PSurfaceAWT.SmoothCanvas) surface.getNative()).getFrame(); //hook into Processing frame
    frame.setTitle("Photo Editor");
    frame.setResizable(true);
    
    View view = new View(frame);
    
    //temporary icon
    surface.setIcon(loadImage("https://image.flaticon.com/icons/png/512/196/196278.png"));
    
    //don't wast resources on a drawing Thread.
    noLoop();
}

//override the default exit function so we can intercept and ask the use to save when they exit
public @Override
void exit() {}

public void forceExit() {
    //used to actually exit the program
    super.exit();
}
class ColorSelector extends JPanel {
    private JColorChooser primary, secondary;
    private Rectangle primaryArea = new Rectangle(),
                     secondaryArea = new Rectangle(),
                     trCorner = new Rectangle(), //top right corner
                     blCorner = new Rectangle(); //bottom left corner
    
    ColorSelector() {
        //setup and intialize JColorChoosers
        primary = new JColorChooser(Color.black);
        secondary = new JColorChooser(Color.white);
        primary.setPreviewPanel(new JPanel());
        secondary.setPreviewPanel(new JPanel());
        setPreferredSize(new Dimension(32, 32));
        
        //listen for mouse clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point mousePos = e.getPoint();
                if (primaryArea.contains(mousePos)) { //click on primary square
                    primary.createDialog((ColorSelector)e.getSource(), "Color Picker (Primary Color)" , true, primary, null, null).setVisible(true);
                } else if (secondaryArea.contains(mousePos)) { //click on secondary square
                    secondary.createDialog((ColorSelector)e.getSource(), "Color Picker (Secondary Color)" , true, secondary, null, null).setVisible(true);
                } else if (blCorner.contains(mousePos)) { //click on set to default square
                    //set primary to black and secondary to white
                    primary.setColor(Color.black);
                    secondary.setColor(Color.white);
                } else if (trCorner.contains(mousePos)) { //click on arrow switch
                    //switch primary and secondary colors
                    Color temp = primary.getColor();
                    primary.setColor(secondary.getColor());
                    secondary.setColor(temp);
                } else {
                    return;
                }
                repaint();
            }
        } );
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //used for the color of the arrows; consistent with the flat laf foreground color
        final Color foreground = new Color(0xADADAD);
        
        Graphics2D g2 = (Graphics2D) g;
        
        //draw the primary color box
        DrawHelper.drawChecker(g2, secondaryArea.x + 2, secondaryArea.y + 2, secondaryArea.width - 3, secondaryArea.height - 3, 4);
        DrawHelper.drawBorderedArea(g2, secondaryArea, Color.black, Color.white, secondary.getColor());
        //draw the secondary color box
        DrawHelper.drawChecker(g2, primaryArea.x + 2, primaryArea.y + 2, primaryArea.width - 3, primaryArea.height - 3, 4);
        DrawHelper.drawBorderedArea(g2, primaryArea,    Color.black, Color.white, primary.getColor());
        
        //scale the component to the desired size
        g2.scale((double)getPreferredSize().width / 32d,(double)getPreferredSize().height / 32d);
        
        //draw the default squares
        DrawHelper.drawBorderedArea(g2, new Rectangle(3, 24, 7, 7), foreground, Color.white);
        DrawHelper.drawBorderedArea(g2, new Rectangle(0, 21, 7, 7), foreground, Color.black);
        
        //draw the arrows
        g.setColor(foreground);
        Polygon arrows = new Polygon(new int[] {28, 23, 23, 21, 23, 23, 28, 28, 26, 28, 30, 28, 28} ,
                                                                new int[] {3, 3, 1, 3, 5, 3, 3, 8, 8, 10, 8, 8, 3} , 13);
        g2.translate(0, - 32 / getPreferredSize().height +.2f); //translate when smaller b/c for some reason it looks misaligned when small
        g.fillPolygon(arrows);
        g.drawPolygon(arrows);
        g2.dispose();
    }
    
    public @Override
    void setPreferredSize(Dimension size) {
        super.setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        //setup the areas that determine where to draw and click
        double scalex = size.width / 32d;
        double scaley = size.height / 32d;
        primaryArea.setRect(0, 0, 20d * scalex, 20d * scaley);
        secondaryArea.setRect(11d * scalex, 11d * scaley, 20d * scalex, 20d * scaley);
        trCorner.setRect(20d * scalex, 0, 12d * scalex, 12d * scaley);
        blCorner.setRect(0, 20d * scaley, 12d * scalex, 12d * scaley);
    }
    
    public void setPrimary(Color c) {
        primary.setColor(c);
        repaint(primaryArea); //repaint only on the parts that need it
    }
    
    public void setSecondary(Color c) {
        secondary.setColor(c);
        repaint(secondaryArea); //repaint only on the parts that need it
    }
    
    public Color getPrimary() {
        return primary.getColor();
    }
    
    public Color getSecondary() {
        return secondary.getColor();
    }
    
    public JColorChooser getPrimaryChooser() {
        return primary;
    }
    
    public JColorChooser getSecondaryChooser() {
        return secondary;
    }
}
public final BlendComposite[] BLEND_MODES = new BlendComposite[] {
    new NormalComposite(),
        null,
        new DarkenComposite(),
        new MultiplyComposite(),
        new ColorBurnComposite(),
        new SubtractiveComposite(),
        null,
        new LightenComposite(),
        new ScreenComposite(),
        new ColorDodgeComposite(),
        new AdditiveComposite(),
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


public abstract class BlendComposite implements Composite, CompositeContext {
    protected int width, height;
    protected int[] srcPixels, dstPixels;
    protected float opacity = 1;
    
    protected float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }
    
    public void setOpacity(float opacity) {
        this.opacity = clamp(opacity);
    }
    
    public float getOpacity() {
        return opacity;
    }
    
    protected void checkType(Raster raster) {
        if (raster.getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
            throw new IllegalStateException("Expected integer sample type");
        }
    }
    
    @Override
    public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
        checkType(src);
        checkType(dstIn);
        checkType(dstOut);
        
        int width = Math.min(src.getWidth(), dstIn.getWidth());
        int height = Math.min(src.getHeight(), dstIn.getHeight());
        int[] srcPixels = ((DataBufferInt)src.getDataBuffer()).getData().clone();
        int[] dstPixels = ((DataBufferInt)dstIn.getDataBuffer()).getData();
        
        for (int i = 0; i < dstPixels.length; i++) {
            srcPixels[i] = blendPixel(srcPixels[i], dstPixels[i]);
            srcPixels[i] = overPixel(srcPixels[i], dstPixels[i]);
        }
        
        dstOut.setDataElements(0, 0, width, height, srcPixels);
    }
    
    protected int toColor(float r, float g, float b, float a) {
        return((int)(255 * a) << 24) |   ((int)(255 * r) << 16) | ((int)(255 * g) << 8) | (int)(255 * b);
    }
    
    protected float alpha(int c) {
        //floats simplify division and multiplication operations
        return(float)((c >> 24) & 0xFF) / 255f;
    }
    
    protected float red(int c) {
        return(float)((c >> 16) & 0xFF) / 255f;
    }
    
    protected float green(int c) {
        return(float)((c >> 8) & 0xFF) / 255f;
    }
    
    protected float blue(int c) {
        return(float)(c & 0xFF) / 255f;
    }
    
    protected int overPixel(int src, int dst) {
        float aSrc = alpha(src);
        float aDst = alpha(dst);
        aSrc *= opacity;
        aDst = aDst * (1 - aSrc);
        
        float a = aSrc + aDst;
        if (a == 0) return 0;
        float r = (red(src) * aSrc + red(dst) * aDst) / a;
        float g = (green(src) * aSrc + green(dst) * aDst) / a;
        float b = (blue(src) * aSrc + blue(dst) * aDst) / a;
        return toColor(r, g, b, a);
    }
    
    @Override
    public CompositeContext createContext(ColorModel srcColorModel, ColorModel dstColorModel, RenderingHints hints) {
        return this;
    }
    
    @Override
    public void dispose() {}
    
    protected abstract int blendPixel(int src, int dst);
}

public abstract class SeparableBlendComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        float r = clamp(blendChannel(red(src), red(dst)));
        float g = clamp(blendChannel(green(src), green(dst)));
        float b = clamp(blendChannel(blue(src), blue(dst)));
        float a = alpha(src);
        
        return toColor(r, g, b, a);
    }
    
    protected abstract float blendChannel(float src, float dst);
}

public class NormalComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return src;
    }
    
    @Override
    public String toString() {
        return "Normal";
    }
}

public class MultiplyComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return src * dst;
    }
    
    @Override
    public String toString() {
        return "Multiply";
    }
}

public class ScreenComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return dst - src - dst * src;
    }
    
    @Override
    public String toString() {
        return "Screen";
    }
}

public class OverlayComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (dst <= 0.5f)
            return src * dst * 2; //multiply
        return 1 - 2 * (1 - src) * (1 - dst); //screen
    }
    
    @Override
    public String toString() {
        return "Overlay";
    }
}

public class DarkenComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return Math.min(src, dst);
    }
    
    @Override
    public String toString() {
        return "Darken";
    }
}

public class LightenComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return Math.max(src, dst);
    }
    
    @Override
    public String toString() {
        return "Lighten";
    }
}

public class AdditiveComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return src + dst;
    }
    
    @Override
    public String toString() {
        return "Additive (Linear Dodge)";
    }
}
public class SubtractiveComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return src + dst - 1;
    }
    
    @Override
    public String toString() {
        return "Subtractive (Linear Burn)";
    }
}

public class ColorDodgeComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src >= 1) return 1;
        return Math.min(1, dst / (1 - src));
    }
    
    @Override
    public String toString() {
        return "Color Dodge";
    }
}

public class ColorBurnComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0) return 0;
        return 1 - Math.min(1,(1 - dst) / src);
    }
    
    @Override
    public String toString() {
        return "Color Burn";
    }
}

public class HardLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0.5f)
            return dst * src * 2; //multiply
        return 1 - 2 * (1 - src) * (1 - dst); //screen
    }
    
    @Override
    public String toString() {
        return "Hard Light";
    }
}

public class SoftLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0.5f)
            return dst - (1 - 2 * src) * dst * (1 - dst);
        return dst + (2 * src - 1) * (d(dst) - dst);
    }
    
    private float d(float x) {
        if (x <=.25f)
            return((16 * x - 12) * x + 4) * x;
        return(float)Math.sqrt(x);
    }
    
    @Override
    public String toString() {
        return "Soft Light";
    }
}

public class VividLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src <= 0.5f)
            return 1 - (1 - dst) / (2 * src);
        return dst / (2 * (1 - src));
    }
    
    @Override
    public String toString() {
        return "Vivid Light";
    }
}

public class LinearLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return dst + 2 * src - 1;
    }
    
    @Override
    public String toString() {
        return "Linear Light";
    }
}

public class PinLightComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (dst < 2 * src - 1)
            return 2 * src - 1;
        if (dst < 2 * src)
            return dst;
        return 2 * src;
    }
    
    @Override
    public String toString() {
        return "Pin Light";
    }
}

public class HardMixComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src < 1 - dst) return 0;
        return 1;
    }
    
    @Override
    public String toString() {
        return "Hard Mix";
    }
}

public class DifferenceComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return Math.abs(dst - src);
    }
    
    @Override
    public String toString() {
        return "Difference";
    }
}

public class ExclusionComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        return dst + src - 2 * dst * src;
    }
    
    @Override
    public String toString() {
        return "Exclusion";
    }
}

public class DivideComposite extends SeparableBlendComposite {
    @Override
    protected float blendChannel(float src, float dst) {
        if (src == 0) return 1;
        return Math.min(1, dst / src);
    }
    
    @Override
    public String toString() {
        return "Divide";
    }
}

public class XorComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return src ^ dst & 0xFFFFFF;
    }
    
    @Override
    public String toString() {
        return "Xor";
    }
}

public class AndComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return dst & 0xFFFFFF & src;
    }
    
    @Override
    public String toString() {
        return "And";
    }
}
public class OrComposite extends BlendComposite {
    @Override
    protected int blendPixel(int src, int dst) {
        return src | dst & 0xFFFFFF;
    }
    
    @Override
    public String toString() {
        return "Or";
    }
}
/** Modified DnDTabbedPane.java
 * http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html
 * originally written by Terai Atsuhiro.
 * so that tabs can be transfered from one pane to another.
 * eed3si9n.
 */















public class DnDTabbedPane extends JTabbedPane {
	private final int LINEWIDTH = 3;
	private final String NAME = "TabTransferData";
	private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
	private GhostGlassPane s_glassPane = new GhostGlassPane();

	private boolean m_isDrawRect = true;
	private final Rectangle2D m_lineRect = new Rectangle2D.Double();

	private final Color m_lineColor = new Color(0, 100, 255);
	public boolean m_acceptor = true;

	public DnDTabbedPane() {
		super();
		final DragSourceListener dsl = new DragSourceListener() {
			public void dragEnter(DragSourceDragEvent e) {
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			public void dragExit(DragSourceEvent e) {
				e.getDragSourceContext()
				.setCursor(DragSource.DefaultMoveNoDrop);
				m_lineRect.setRect(0, 0, 0, 0);
				m_isDrawRect = false;
				s_glassPane.setPoint(new Point(-1000, -1000));
				s_glassPane.repaint();
			}

			public void dragOver(DragSourceDragEvent e) {
				//e.getLocation()
				//This method returns a Point indicating the cursor location in screen coordinates at the moment
				TabTransferData data = getTabTransferData(e);
				if (data == null) {
					e.getDragSourceContext().setCursor(
						DragSource.DefaultMoveNoDrop);
					return;
				}

				e.getDragSourceContext().setCursor(
					DragSource.DefaultMoveDrop);
			}

			public void dragDropEnd(DragSourceDropEvent e) {
				m_isDrawRect = true;
				m_lineRect.setRect(0, 0, 0, 0);
				// m_dragTabIndex = -1;

				if (hasGhost()) {
					s_glassPane.setVisible(false);
					s_glassPane.setImage(null);
				}

        if(!e.getDropSuccess()){
            // MenuLight class Extends JFrame and Included 1 component JTabbedPane called superPane
            DnDTabbedPane windowTabbedPane = new DnDTabbedPane();
            windowTabbedPane.add(new JTree());
            JFrame f = new JFrame();
            //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            //f.setUndecorated(true);
            f.setSize(new Dimension(200, 200));
            f.setContentPane(windowTabbedPane);
            f.setVisible(true);

            // after create Frame, transfer the tab to other jtabbedpane
            //windowTabbedPane.convertTab(getTabTransferData(e), getTargetTabIndex(e.getLocation()));
        }

        // if current JTabbedPane Tab is empty dispose it.
        if(getTabCount() < 1){
          println(getParent() instanceof JLayeredPane);
        }

			}

			public void dropActionChanged(DragSourceDragEvent e) {
			}
		};

		final DragGestureListener dgl = new DragGestureListener() {
			public void dragGestureRecognized(DragGestureEvent e) {
				//println("dragGestureRecognized");

				Point tabPt = e.getDragOrigin();
				int dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
				if (dragTabIndex < 0) {
					return;
				}

				initGlassPane(e.getComponent(), e.getDragOrigin(), dragTabIndex);
				try {
					e.startDrag(DragSource.DefaultMoveDrop, new TabTransferable(DnDTabbedPane.this, dragTabIndex), dsl);
				} catch (InvalidDnDOperationException idoe) {
					idoe.printStackTrace();
				}
			}
		};

		//dropTarget =
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
	}

	private TabTransferData getTabTransferData(DropTargetDropEvent event) {
		try {return (TabTransferData) event.getTransferable().getTransferData(FLAVOR);}
		catch (Exception e) {return null;}
	}
	private TabTransferData getTabTransferData(DropTargetDragEvent event) {
		try {return (TabTransferData) event.getTransferable().getTransferData(FLAVOR);}
		catch (Exception e) {return null;}
	}
	private TabTransferData getTabTransferData(DragSourceDragEvent event) {
		try {return (TabTransferData) event.getDragSourceContext().getTransferable().getTransferData(FLAVOR);}
		catch (Exception e) {return null;}
	}

	class TabTransferable implements Transferable {
		private TabTransferData m_data = null;

		public TabTransferable(DnDTabbedPane a_tabbedPane, int a_tabIndex) {
			m_data = new TabTransferData(DnDTabbedPane.this, a_tabIndex);
		}

		public Object getTransferData(DataFlavor flavor) {
			return m_data;
			// return DnDTabbedPane.this;
		}

		public DataFlavor[] getTransferDataFlavors() {
			DataFlavor[] f = new DataFlavor[1];
			f[0] = FLAVOR;
			return f;
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.getHumanPresentableName().equals(NAME);
		}
	}

	class TabTransferData {
		private DnDTabbedPane m_tabbedPane = null;
		private int m_tabIndex = -1;

		public TabTransferData() {
		}

		public TabTransferData(DnDTabbedPane a_tabbedPane, int a_tabIndex) {
			m_tabbedPane = a_tabbedPane;
			m_tabIndex = a_tabIndex;
		}

		public DnDTabbedPane getImageTabs() {
			return m_tabbedPane;
		}

		public void setTabbedPane(DnDTabbedPane pane) {
			m_tabbedPane = pane;
		}

		public int getTabIndex() {
			return m_tabIndex;
		}

		public void setTabIndex(int index) {
			m_tabIndex = index;
		}
	}

	private Point buildGhostLocation(Point a_location) {
		Point retval = new Point(a_location);

		switch (getTabPlacement()) {
		case JTabbedPane.TOP: {
			retval.y = 1;
			retval.x -= s_glassPane.getGhostWidth() / 2;
		} break;

		case JTabbedPane.BOTTOM: {
			retval.y = getHeight() - 1 - s_glassPane.getGhostHeight();
			retval.x -= s_glassPane.getGhostWidth() / 2;
		} break;

		case JTabbedPane.LEFT: {
			retval.x = 1;
			retval.y -= s_glassPane.getGhostHeight() / 2;
		} break;

		case JTabbedPane.RIGHT: {
			retval.x = getWidth() - 1 - s_glassPane.getGhostWidth();
			retval.y -= s_glassPane.getGhostHeight() / 2;
		} break;
		}

		retval = SwingUtilities.convertPoint(DnDTabbedPane.this, retval, s_glassPane);
		return retval;
	}

	class CDropTargetListener implements DropTargetListener {
		public void dragEnter(DropTargetDragEvent e) {
			//println("DropTarget.dragEnter: " + DnDTabbedPane.this);

			if (isDragAcceptable(e)) {
				e.acceptDrag(e.getDropAction());
			} else {
				e.rejectDrag();
			}
		}

		public void dragExit(DropTargetEvent e) {
			//println("DropTarget.dragExit: " + DnDTabbedPane.this);
			m_isDrawRect = false;
		}

		public void dropActionChanged(DropTargetDragEvent e) {
		}

		public void dragOver(final DropTargetDragEvent e) {
			TabTransferData data = getTabTransferData(e);

			if (getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM) {
				initTargetLeftRightLine(getTargetTabIndex(e.getLocation()), data);
			} else {
				initTargetTopBottomLine(getTargetTabIndex(e.getLocation()), data);
			}

			repaint();
			if (hasGhost()) {
				s_glassPane.setPoint(buildGhostLocation(e.getLocation()));
				s_glassPane.repaint();
			}
		}

		public void drop(DropTargetDropEvent a_event) {
			// System.out.println("DropTarget.drop: " + DnDTabbedPane.this);

			if (isDropAcceptable(a_event)) {
				convertTab(getTabTransferData(a_event),
				           getTargetTabIndex(a_event.getLocation()));
				a_event.dropComplete(true);
			} else {
				a_event.dropComplete(false);
			}

			m_isDrawRect = false;
			repaint();
		}

		public boolean isDragAcceptable(DropTargetDragEvent e) {

			Transferable t = e.getTransferable();
			DataFlavor[] flavor = e.getCurrentDataFlavors();
			TabTransferData data = getTabTransferData(e);

			if (t == null) return false;
			if (!t.isDataFlavorSupported(flavor[0])) return false;
			if (DnDTabbedPane.this == data.getImageTabs() && data.getTabIndex() >= 0) return true;
			if (DnDTabbedPane.this != data.getImageTabs()) return m_acceptor;

			return false;
		}

		public boolean isDropAcceptable(DropTargetDropEvent e) {
			Transferable t = e.getTransferable();
			DataFlavor[] flavor = e.getCurrentDataFlavors();
			TabTransferData data = getTabTransferData(e);

			if (t == null) return false;
			if (!t.isDataFlavorSupported(flavor[0])) return false;
			if (DnDTabbedPane.this == data.getImageTabs() && data.getTabIndex() >= 0) return true;
			if (DnDTabbedPane.this != data.getImageTabs()) return m_acceptor;

			return false;
		}
	}

	private boolean m_hasGhost = true;

	public void setPaintGhost(boolean flag) {
		m_hasGhost = flag;
	}

	public boolean hasGhost() {
		return m_hasGhost;
	}

/**
 * returns potential index for drop.
 * @param a_point point given in the drop site component's coordinate
 * @return returns potential index for drop.
 */
	private int getTargetTabIndex(Point a_point) {
		boolean isTopOrBottom = getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM;

		// if the pane is empty, the target index is always zero.
		if (getTabCount() == 0) {
			return 0;
		}

		for (int i = 0; i < getTabCount(); i++) {
			Rectangle r = getBoundsAt(i);
			if (isTopOrBottom) {
				r.setRect(r.x - r.width / 2, r.y, r.width, r.height);
			} else {
				r.setRect(r.x, r.y - r.height / 2, r.width, r.height);
			}

			if (r.contains(a_point)) {
				return i;
			}
		}

		Rectangle r = getBoundsAt(getTabCount() - 1);
		if (isTopOrBottom) {
			int x = r.x + r.width / 2;
			r.setRect(x, r.y, getWidth() - x, r.height);
		} else {
			int y = r.y + r.height / 2;
			r.setRect(r.x, y, r.width, getHeight() - y);
		}

		return r.contains(a_point) ? getTabCount() : -1;
	}

	private void convertTab(TabTransferData a_data, int a_targetIndex) {
		DnDTabbedPane source = a_data.getImageTabs();
		int sourceIndex = a_data.getTabIndex();
		if (sourceIndex < 0) {
			return;
		}

		Component cmp = source.getComponentAt(sourceIndex);
		String str = source.getTitleAt(sourceIndex);
		if (this != source) {
			source.remove(sourceIndex);

			if (a_targetIndex == getTabCount()) {
				addTab(str, cmp);
			} else {
				if (a_targetIndex < 0) {
					a_targetIndex = 0;
				}

				insertTab(str, null, cmp, null, a_targetIndex);

			}

			setSelectedComponent(cmp);
			// System.out.println("press="+sourceIndex+" next="+a_targetIndex);
			return;
		}

		if (a_targetIndex < 0 || sourceIndex == a_targetIndex) {
			//System.out.println("press="+prev+" next="+next);
			return;
		}

		if (a_targetIndex == getTabCount()) {
			//System.out.println("last: press="+prev+" next="+next);
			source.remove(sourceIndex);
			addTab(str, cmp);
			setSelectedIndex(getTabCount() - 1);
		} else if (sourceIndex > a_targetIndex) {
			//System.out.println("   >: press="+prev+" next="+next);
			source.remove(sourceIndex);
			insertTab(str, null, cmp, null, a_targetIndex);
			setSelectedIndex(a_targetIndex);
		} else {
			//System.out.println("   <: press="+prev+" next="+next);
			source.remove(sourceIndex);
			insertTab(str, null, cmp, null, a_targetIndex - 1);
			setSelectedIndex(a_targetIndex - 1);
		}
	}

	private void initTargetLeftRightLine(int next, TabTransferData a_data) {
		if (next < 0) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
			return;
		}

		if ((a_data.getImageTabs() == this) && (a_data.getTabIndex() == next || next - a_data.getTabIndex() == 1)) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
		} else if (getTabCount() == 0) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
			return;
		} else if (next == 0) {
			Rectangle rect = getBoundsAt(0);
			m_lineRect.setRect(-LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
			m_isDrawRect = true;
		} else if (next == getTabCount()) {
			Rectangle rect = getBoundsAt(getTabCount() - 1);
			m_lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
			m_isDrawRect = true;
		} else {
			Rectangle rect = getBoundsAt(next - 1);
			m_lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
			m_isDrawRect = true;
		}
	}

	private void initTargetTopBottomLine(int next, TabTransferData a_data) {
		if (next < 0) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
			return;
		}

		if ((a_data.getImageTabs() == this)
		    && (a_data.getTabIndex() == next
		        || next - a_data.getTabIndex() == 1)) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
		} else if (getTabCount() == 0) {
			m_lineRect.setRect(0, 0, 0, 0);
			m_isDrawRect = false;
			return;
		} else if (next == getTabCount()) {
			Rectangle rect = getBoundsAt(getTabCount() - 1);
			m_lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
			m_isDrawRect = true;
		} else if (next == 0) {
			Rectangle rect = getBoundsAt(0);
			m_lineRect.setRect(rect.x, -LINEWIDTH / 2, rect.width, LINEWIDTH);
			m_isDrawRect = true;
		} else {
			Rectangle rect = getBoundsAt(next - 1);
			m_lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
			m_isDrawRect = true;
		}
	}

	private void initGlassPane(Component c, Point tabPt, int a_tabIndex) {
		//Point p = (Point) pt.clone();
		getRootPane().setGlassPane(s_glassPane);
		if (hasGhost()) {
			Rectangle rect = getBoundsAt(a_tabIndex);
			BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			c.paint(g);
			image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
			s_glassPane.setImage(image);
		}

		s_glassPane.setPoint(buildGhostLocation(tabPt));
		s_glassPane.setVisible(true);
	}

	private Rectangle getTabAreaBound() {
		Rectangle lastTab = getUI().getTabBounds(this, getTabCount() - 1);
		return new Rectangle(0, 0, getWidth(), lastTab.y + lastTab.height);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (m_isDrawRect) {
			Graphics2D g2 = (Graphics2D) g;
			//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			g2.setPaint(m_lineColor);
			g2.fill(m_lineRect);
		}
	}
}

class GhostGlassPane extends JPanel {
	public final long serialVersionUID = 1L;
	private final AlphaComposite m_composite;

	private Point m_location = new Point(0, 0);

	private BufferedImage m_draggingGhost = null;

	public GhostGlassPane() {
		setOpaque(false);
		m_composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
	}

	public void setImage(BufferedImage draggingGhost) {
		m_draggingGhost = draggingGhost;
	}

	public void setPoint(Point a_location) {
		m_location.x = a_location.x;
		m_location.y = a_location.y;
	}

	public int getGhostWidth() {
		if (m_draggingGhost == null) {
			return 0;
		}

		return m_draggingGhost.getWidth(this);
	}

	public int getGhostHeight() {
		if (m_draggingGhost == null) {
			return 0;
		}

		return m_draggingGhost.getHeight(this);
	}

	public void paintComponent(Graphics g) {
		if (m_draggingGhost == null) {
			return;
		}

		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(m_composite);
		g2.drawImage(m_draggingGhost, (int) m_location.getX(), (int) m_location.getY(), null);
	}
}
public class Document {
    ArrayList<Layer> layers = new ArrayList<Layer>();
    private int height, width;
    private boolean isSaved = true;
    private BufferedImage flattened;
    private File linkedFile;
    
    Document(int width, int height) {
        this.width = width;
        this.height = height;
        Layer layer = new Layer(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        layers.add(layer);
    }
    
    Document(File file) {
        linkedFile = file;
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch(IOException e) {
            JOptionPane.showMessageDialog(null, "Something went wrong when trying to read your file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        width = image.getWidth();
        height = image.getHeight();
        layers.add(new Layer(toRGBA(image)));
    }
    
    Document(BufferedImage image) {
        this.width = image.getWidth();
        this.height = image.getHeight();
        layers.add(new Layer(toRGBA(image)));
    }
    
    private BufferedImage toRGBA(BufferedImage image) {
        BufferedImage rbgaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = rbgaImage.createGraphics();
        g2.drawImage(image, null, 0, 0);
        g2.dispose();
        return rbgaImage;
    }
    
    public String getName() {
        if (linkedFile != null) {
            return linkedFile.getName();
        }
        return "Untitled";
    }
    
    public BufferedImage flattened() {
        if (flattened == null) updateFlattenedCache();
        return flattened;
    }
    
    public void updateFlattenedCache() {
        //a real bottleneck for performance but it'll have to do for now, perhaps cache the preflattened layer below
        flattened = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = flattened.createGraphics();
        for (Layer layer : layers) {
            if (!layer.isVisible()) continue;
            BlendComposite blendComposite = layer.getBlendComposite();
            blendComposite.setOpacity(layer.getOpacity());
            g2.setComposite(blendComposite);
            g2.drawImage(layer.getImage(), null, 0, 0);
        }
        g2.dispose();
    }
    
    public boolean isSaved() {
        return isSaved;
    }
    
    public void setSaved(boolean value) {
        isSaved = value;
    }
    
    public boolean isLinked() {
        return linkedFile != null;
    }
    
    public File getLinkedFile() {
        return linkedFile;
    }
    
    public void setLinkedFile(File file) {
        this.linkedFile = file;
    }
    
    public ArrayList<Layer> getLayers() {
        return layers;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public Dimension getDimension() {
        return new Dimension(width, height);
    }
    
    public Layer addEmptyLayer(int index) {
        Layer layer = new Layer(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        layer.setName(String.format("Layer %d", layers.size()));
        layers.add(index, layer);
        return layer;
    }
    
    public Layer addEmptyLayer() {
        return addEmptyLayer(layers.size());
    }
    
    public void addLayer(Layer layer, int index) {
        layers.add(index, layer);
    }
    
    public void addLayer(Layer layer) {
        layers.add(layer);
    }
    
    public void crop(Rectangle rect) {
        for (Layer layer : layers) {
            layer.crop(rect);
        }
        width = rect.width;
        height = rect.height;
    }
}

public class SnapShot {
    private ArrayList<Layer> layers = new ArrayList<Layer>();
    private int height, width;
    private int selectedLayer;
    
    SnapShot(Document doc, int selectedLayer) {
        height = doc.getHeight();
        width = doc.getWidth();
        for (Layer layer : doc.getLayers()) {
            layers.add(layer.copy());
        }
        this.selectedLayer = selectedLayer;
    }
    
    public ArrayList<Layer> getLayers() {
        return layers;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getSelectedLayer() {
        return selectedLayer;
    }
}

public class SnapShotManager {
    Document doc;
    DocumentView docView;
    ArrayDeque<SnapShot> undoHistory = new ArrayDeque<SnapShot>();
    ArrayDeque<SnapShot> redoHistory = new ArrayDeque<SnapShot>();
    
    SnapShotManager(DocumentView docView) {
        this.docView = docView;
        this.doc = docView.getDocument();
    }
    
    public void save(SnapShot snapshot) {
        redoHistory.clear();
        undoHistory.push(snapshot);
    }
    
    public void save() {
        redoHistory.clear();
        undoHistory.push(new SnapShot(doc, docView.getSelectedLayerIndex()));
        println(undoHistory);
    }
    
    public void undo() {
        if (!ableToUndo())return;
        SnapShot save = undoHistory.pop();
        redoHistory.push(new SnapShot(doc, docView.getSelectedLayerIndex()));
        restore(save);
    }
    
    public void redo() {
        if (!ableToRedo()) return;
        SnapShot save = redoHistory.pop();
        undoHistory.push(new SnapShot(doc, docView.getSelectedLayerIndex()));
        restore(save);
    }
    
    private void restore(SnapShot save) {
        ArrayList<Layer> layers = doc.getLayers();
        layers.clear();
        layers.addAll(save.getLayers());
        doc.setHeight(save.getHeight());
        doc.setWidth(save.getWidth());
        docView.setSelectedLayerIndex(save.getSelectedLayer());
        docView.revalidate();
        docView.repaint();
    }
    
    public boolean ableToUndo() {
        return !undoHistory.isEmpty();
    }
    
    public boolean ableToRedo() {
        return !redoHistory.isEmpty();
    }
}

public class Layer {
    BufferedImage image;
    private String name = "Background";
    private boolean visibility = true;
    private float opacity = 1f;
    private Graphics2D g;
    private int blendIndex = 0;
    
    Layer(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            throw new IllegalStateException("Expected TYPE_INT_ARGB");
        }
        this.image = image;
        g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public void crop(Rectangle rect) {
        g.dispose();
        BufferedImage crop = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
        image = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        g.drawImage(crop, 0, 0, null);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public BlendComposite getBlendComposite() {
        return BLEND_MODES[blendIndex];
    }
    
    public int getBlendIndex() {
        return blendIndex;
    }
    
    public void setBlendComposite(int index) {
        blendIndex = index;
    }
    
    public boolean isVisible() {
        return visibility;
    }
    
    public void setVisible(boolean visibility) {
        this.visibility = visibility;
    }
    
    public float getOpacity() {
        return opacity;
    }
    
    public void setOpacity(float opacity) {
        this.opacity = Math.max(0, Math.min(1, opacity));
    }
    
    public Graphics2D getGraphics() {
        return g;
    }
    
    public Layer copy() {
        ColorModel colorModel = image.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(image.getRaster().createCompatibleWritableRaster());
        BufferedImage imageCopy = new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
        Layer copy = new Layer(imageCopy);
        copy.setName(name);
        copy.setOpacity(opacity);
        copy.setVisible(isVisible());
        copy.setBlendComposite(blendIndex);
        return copy;
    }
}
public class DocumentView extends JPanel {
    public final float[] ZOOM_TABLE = {2,3,4,5,6,7,8,9,10,12.5f,17,20,25,33.33f,50,66.67f,100,150,200,300,400,500,600,800,1000,1200,1400,1600,2000,2400,3200,4000,4800,5600,6400};
    private final int INFOBAR_HEIGHT = 24;
    private Thread selectionAnimator;
    private float scale = 1;
    
    private View view;
    private Document document;
    private int selectedLayerIndex;
    private Shape selection;
    
    private JPanel infoBar;
    private JLabel toolTipLabel;
    private JLabel imageSizeLabel;
    private JLabel positionLabel;
    private JButton fitToWindow, zoomOut, zoomIn;
    private JSpinner zoomSpinner;
    private JSlider zoomSlider;
    
    private JScrollPane scrollPane;
    private JViewport viewport;
    private JPanel canvasWrapper;
    private Canvas canvas;
    
    private SnapShotManager snapShotManager;
    
    DocumentView(Document document, View view) {
        this.view = view;
        this.document = document;
        this.snapShotManager = new SnapShotManager(this);
        this.selectedLayerIndex = 0;
        setLayout(new BorderLayout());
        
        toolTipLabel = new JLabel((ImageIcon) view.getToolBar().getSelectedTool().getValue(Action.SMALL_ICON));
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
        return new ImageIcon(sketchPath(String.format("resources/infoBar/%s", string)));
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
        Dimension size = new Dimension(separator.getPreferredSize().width, separator.getMaximumSize().height);
        separator.setMaximumSize(size);
        infoBar.add(separator);
        infoBar.add(Box.createRigidArea(new Dimension(5, INFOBAR_HEIGHT)));
    }
    
    private void setupZoomControl() {
        JButton[] buttons = {
            fitToWindow = new JButton(new ZoomToWindowAction(view)),
                zoomOut = new JButton(new ZoomOutAction(view)),
                zoomIn = new JButton(new ZoomInAction(view))
            };
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
        }
        
        fitToWindow.setIcon(infoBarIcon("fitToWindow.png"));
        zoomOut.setIcon(infoBarIcon("zoomOut.png"));
        zoomIn.setIcon(infoBarIcon("zoomIn.png"));
        
        zoomSpinner = new JSpinner(new SpinnerNumberModel(100d, 1d, 6400d, 1d));
        Dimension zoomSpinnerSize = new Dimension(45, INFOBAR_HEIGHT);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(zoomSpinner, "##0.##");
        editor.getTextField().setBackground(null);
        zoomSpinner.setUI(new BasicSpinnerUI() {
            protected Component createNextButton() {
                return null;
            }
            protected Component createPreviousButton() {
                return null;
            }
        });
        zoomSpinner.setMaximumSize(editor.getTextField().getPreferredSize());
        zoomSpinner.setEditor(editor);
        zoomSpinner.setBorder(null);
        zoomSpinner.setBackground(null);
        zoomSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                float value = ((Number)zoomSpinner.getValue()).floatValue();
                setScale(value / 100);
            }
        });
        
        int i;
        for (i = 0; i < ZOOM_TABLE.length; i++) {
            if (ZOOM_TABLE[i] == 100) break;
        }
        zoomSlider = new JSlider(JSlider.HORIZONTAL, 0, ZOOM_TABLE.length - 1, i);
        Dimension sliderSize = new Dimension(110, INFOBAR_HEIGHT);
        zoomSlider.setPreferredSize(sliderSize);
        zoomSlider.setMinimumSize(sliderSize);
        zoomSlider.setMaximumSize(sliderSize);
        zoomSlider.setSnapToTicks(true);
        zoomSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider)e.getSource();
                if (source.getValueIsAdjusting())
                    setScale(ZOOM_TABLE[source.getValue()] / 100);
            }
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
        
        scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
        
        viewport = scrollPane.getViewport();
        viewport.add(canvasWrapper);
        viewport.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                viewport.repaint();
            }
        });
    }
    
    private class Canvas extends JPanel {
        final Stroke singleWhiteDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1} , 0);
        final Stroke singleBlackDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1} , 1);
        public Stroke selectionWhiteDash = new BasicStroke();
        public Stroke selectionBlackDash = new BasicStroke();
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g.create();
            
            Rectangle viewRect = viewport.getViewRect();
            int deltax = viewRect.x % 16;
            int deltay = viewRect.y % 16;
            DrawHelper.drawChecker(g2, viewRect.x - deltax, viewRect.y - deltay, viewRect.width + deltax - 1, viewRect.height + deltay - 1, 8);
            AffineTransform preScale = g2.getTransform();
            g2.scale(scale, scale);
            g2.drawImage(document.flattened(), 0, 0, null);
            g2.setTransform(preScale);
            
            if (view.isPixelGridEnabled() && (viewRect.height + viewRect.width) / scale < 50) { //have to limit at 50 lines idk how to make it faster
                int startingRow = PApplet.parseInt(viewRect.y / scale);
                for (int row = startingRow + 1; row < startingRow + viewRect.height / scale + 1; row ++) {
                    int y = (int)(scale * row);
                    drawDoubleDashed(g2, new Line2D.Float(viewRect.x, y, viewRect.x + viewRect.width, y));
                }
                int startingCol = PApplet.parseInt(viewRect.x / scale);
                for (int col = startingCol + 1; col < startingCol + viewRect.width / scale + 1; col ++) {
                    int x = (int)(scale * col);
                    drawDoubleDashed(g2, new Line2D.Float(x, viewRect.y, x, viewRect.y + viewRect.height));
                }
            }
            
            //hacky way of showing selection on bottom and right edges consistently
            g2.setColor(view.CONTENT_BACKGROUND);
            Dimension size = getPreferredSize();
            g2.drawLine(--size.width, 0, size.width, --size.height);
            g2.drawLine(0, size.height, size.width, size.height);
            
            
            if (hasSelection()) {
                drawDoubleDashed(g2, getScaledSelection(), selectionWhiteDash, selectionBlackDash);
            }
            g2.dispose();
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
            return new Dimension(
                round(document.getWidth() * scale + 1),
                round(document.getHeight() * scale + 1)
               );
        }
        
        public boolean largerThan(Dimension container) {
            return getPreferredSize().width > container.getWidth() ||
                getPreferredSize().height > container.getHeight();
        }
    }
    
    private class CanvasMouseListener extends MouseAdapter {
        Point2D pos = new Point2D.Double(0, 0);
        ToolAction tool;
        DragGesture dragState;
        Point origin;
        
        public @Override
        void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isControlDown()) {
                setScale(constrain(scale * pow(1.1f, - e.getWheelRotation()),.01f, 64), e.getPoint());
                return;
            }
            Rectangle view = viewport.getViewRect();
            int delta = e.getWheelRotation() * 50;
            if (e.isShiftDown()) {
                view.x += delta;
            } else {
                view.y += delta;
            }
            
           ((JPanel)viewport.getView()).scrollRectToVisible(view);
        }
        
        public @Override
        void mouseMoved(MouseEvent e) {
            updatePos(e);
            updatePostionLabel();
        }
        
        public @Override
        void mouseDragged(MouseEvent e) {
            updatePos(e);
            dragState.dragTo(pos);
            tool.dragging();
            canvas.repaint();
            updatePostionLabel();
        }
        
        public @Override
        void mousePressed(MouseEvent e) {
            origin = e.getPoint();
            updatePos(e);
            if (!dragState.isDragging()) {
                dragState.start(pos, e.getButton());
            } else {
                dragState.pressButton(e.getButton());
            }
            tool.dragStarted();
            tool.dragging();
        }
        
        public @Override
        void mouseReleased(MouseEvent e) {
            updatePos(e);
            dragState.releaseButton(e.getButton());
            if (dragState.getButtons().isEmpty()) {
                dragState.stop(pos);
            }
            tool.dragEnded();
        }
        
        public void updatePos(MouseEvent e) {
            Point mouse = e.getPoint();
            tool = view.getToolBar().getSelectedTool();
            toolTipLabel.setText(tool.getToolTip());
            dragState = tool.getDragState();
            JComponent source = (JComponent)e.getSource();
            if (source.getLayout() instanceof GridBagLayout) { //wrapper
                Component canvas = source.getComponent(0);
                mouse.translate(- (source.getWidth() - canvas.getWidth()) / 2, - (source.getHeight() - canvas.getHeight()) / 2);
            }
            pos.setLocation(mouse.x / scale, mouse.y / scale);
        }
        
        public @Override
        void mouseClicked(MouseEvent e) {
            updatePos(e);
            tool.click(pos, e.getButton());
        }
        
        private void updatePostionLabel() {
            positionLabel.setText(String.format("%d, %dpx",(int) pos.getX(),(int) pos.getY()));
        }
    }
    
    private class SelectionAnimator extends Thread {
        DocumentView docView;
        
        SelectionAnimator(DocumentView docView) {
            this.docView = docView;
        }
        
        public void run() {
            int cycle = 0;
            while(docView.hasSelection()) {
                Canvas canvas = docView.getCanvas();
                canvas.selectionBlackDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3} , 5 - cycle);
                canvas.selectionWhiteDash = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3} , 5 - ((cycle + 3) % 6));
                Rectangle bounds = docView.getScaledSelection().getBounds();
                bounds.grow(1, 1); //confirm entire selection is repainted
                canvas.repaint(bounds);
                cycle = (cycle + 1) % 6;
                try {
                    Thread.sleep(100);
                } catch(Exception e) {}
            }
        }
    }
    
    public Shape getSelection() {
        if (selection == null) return new Rectangle2D.Double(0, 0, document.getWidth(), document.getHeight());
        return selection;
    }
    
    public Shape getScaledSelection() {
        if (selection == null) return new Rectangle2D.Double(0, 0, document.getWidth() * scale, document.getHeight() * scale);
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        return transform.createTransformedShape(selection);
    }
    
    public void setSelection(Shape selection) {
        this.selection = selection;
        canvas.repaint();
        if (selectionAnimator == null || !selectionAnimator.isAlive() && hasSelection()) {
            selectionAnimator = new SelectionAnimator(this);
            selectionAnimator.start();
        }
    }
    
    public boolean hasSelection() {
        return selection != null;
    }
    
    public void removeSelection() {
        this.selection = null;
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
        //if canvas is smaller than viewport, no need to translate the view position
        if (canvas.largerThan(viewport.getExtentSize())) {
            Point viewPos = viewport.getViewPosition();
            viewport.setViewPosition(new Point(
               (int)Math.round(viewPos.x + pos.getX() * deltaScale - pos.getX()),
               (int)Math.round(viewPos.y + pos.getY() * deltaScale - pos.getY())
               )
               );
        }
        
        int lastTick = 0;
        while(lastTick < ZOOM_TABLE.length && scale >= ZOOM_TABLE[lastTick] / 100) {
            lastTick++;
        }
        if (!zoomSlider.getValueIsAdjusting())
            zoomSlider.setValue(lastTick);
        zoomSpinner.setValue(scale * 100);
        
        canvas.revalidate();
        viewport.repaint();
    }
    
    public void setScale(float scale) {
        //default to center of viewrect
        Rectangle viewRect = viewport.getViewRect();
        setScale(scale, new Point(viewRect.x + viewRect.width / 2, viewRect.y + viewRect.height / 2));
    }
    
    public Layer getSelectedLayer() {
        return document.getLayers().get(selectedLayerIndex);
    }
    
    public void setSelectedLayer(Layer layer) {
        selectedLayerIndex = document.getLayers().indexOf(layer);
    }
    
    public void setSelectedLayerIndex(int index) {
        selectedLayerIndex = index;
    }
    
    public int getSelectedLayerIndex() {
        return selectedLayerIndex;
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
        imageSizeLabel.setText(String.format("%d x %dpx", document.getWidth(), document.getHeight()));
    }
    
    public SnapShotManager getSnapShotManager() {
        return snapShotManager;
    }
    
    public void save() {
        snapShotManager.save();
    }
}
public static class DrawHelper {
    public final static Color CHECKER = new Color(0xCCCCCC);
    
    public static void drawChecker(Graphics2D g, int x, int y, int width, int height, int size) {
        g.setColor(Color.white);
        g.fillRect(x, y, width, height);
        g.setColor(new Color(0xCCCCCC));
        for (int i = 0; i < height; i += size) {
            for (int j = 0; j < width; j += size * 2) {
                int squareX = x + j + i % (2 * size);
                int squareY = y + i;
                g.fillRect(squareX, squareY, Math.max(0, size - Math.max(0, squareX + size - x - width)), Math.max(0, size - Math.max(0, squareY + size - y - height)));
            }
        }
    }
    
    public static void drawBorderedArea(Graphics2D g, Rectangle area, Color...fill) {
        area = (Rectangle) area.clone();
        area.width--;
        area.height--;
        for (Color c : fill) {
            g.setPaint(c);
            g.draw(area);
            area.grow( - 1, - 1);
        }
        area.width++;
        area.height++;
        g.fill(area);
    }
}
public class PopArtFilter extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {}
}

public class WarholBiggie extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {}
}

public class FantasyFilter extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {}
}
public abstract class LayerAction extends AbstractAction {
    protected LayerListView list;
    protected DocumentView docView;
    protected Document doc;
    protected ArrayList layers;
    protected int selectedIndex;
    protected Layer selectedLayer;
    
    LayerAction(String layerActionIconName, LayerListView layerListView) {
        this.list = layerListView;
        putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/layers/actions/%s", layerActionIconName))));
    }
    
    protected void initVars() {
        docView = list.getView().getSelectedDocumentView();
        doc = docView.getDocument();
        layers = doc.getLayers();
        selectedLayer = docView.getSelectedLayer();
        selectedIndex = docView.getSelectedLayerIndex();
    }
    
    @Override
    public boolean isEnabled() {
        enabled = list.getView().hasSelectedDocument();
        if (enabled) initVars();
        return enabled;
    }
}

public class addEmptyLayer extends LayerAction {
    addEmptyLayer(LayerListView layerListView) {
        super("add.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
        Layer layer = doc.addEmptyLayer(++selectedIndex);
        docView.setSelectedLayerIndex(selectedIndex);
        list.update();
    }
}

public class RemoveLayer extends LayerAction {
    RemoveLayer(LayerListView layerListView) {
        super("remove.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
        layers.remove(selectedIndex);
        docView.setSelectedLayer((Layer)layers.get(Math.max(selectedIndex - 1, 0)));
        list.update();
    }
    
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && layers.size() > 1;
    }
}

public class DuplicateLayer extends LayerAction {
    DuplicateLayer(LayerListView layerListView) {
        super("duplicate.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
        Layer copy = selectedLayer.copy();
        doc.addLayer(copy, selectedIndex);
        docView.setSelectedLayerIndex(selectedIndex + 1);
        list.update();
    }
}

public class MergeLayer extends LayerAction {
    MergeLayer(LayerListView layerListView) {
        super("merge.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
        Layer below = doc.getLayers().get(selectedIndex - 1);
        Graphics2D g = below.getGraphics();
        Composite before = g.getComposite();
        BlendComposite blendComposite = selectedLayer.getBlendComposite();
        blendComposite.setOpacity(selectedLayer.getOpacity());
        g.setComposite(blendComposite);
        g.drawImage(selectedLayer.getImage(), null, 0, 0);
        g.setComposite(before); //restore
        layers.remove(selectedLayer);
        docView.setSelectedLayer(below);
        list.update();
    }
    
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && selectedIndex > 0;
    }
}

public class MoveUpLayer extends LayerAction {
    MoveUpLayer(LayerListView layerListView) {
        super("moveUp.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
        Collections.swap(layers, selectedIndex, selectedIndex + 1);
        docView.setSelectedLayerIndex(selectedIndex + 1);
        list.update();
    }
    
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && selectedIndex + 1 < layers.size();
    }
}

public class MoveDownLayer extends LayerAction {
    MoveDownLayer(LayerListView layerListView) {
        super("moveDown.png", layerListView);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) return;
        docView.save();
        Collections.swap(layers, selectedIndex, selectedIndex - 1);
        docView.setSelectedLayerIndex(selectedIndex - 1);
        list.update();
    }
    
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && selectedIndex > 0;
    }
}
public class LayerListView extends JPanel implements ChangeListener, ActionListener, DocumentListener {
    private View view;
    private JPanel layerList;
    private GridBagConstraints layerConstraint = new GridBagConstraints();
    private ButtonGroup layerGroup = new ButtonGroup();
    private JTextField nameField;
    private JComboBox blendComboBox;
    private JSlider opacitySlider;
    private JSpinner opacitySpinner;
    private JPanel layerActionsPanel;
    private LayerAction[] layerActions = new LayerAction[] {
        new addEmptyLayer(this),
            new RemoveLayer(this),
            new DuplicateLayer(this),
            new MergeLayer(this),
            new MoveUpLayer(this),
            new MoveDownLayer(this)
        };
    
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
        nameProperty.add(new JLabel("Name:     "));
        nameField = new JTextField();
        nameField.getDocument().addDocumentListener(this);
        nameProperty.add(nameField);
        nameProperty.setBorder(padding);
        
        //defining the blend property
        JPanel blendProperty = new JPanel();
        blendProperty.setLayout(new BoxLayout(blendProperty, BoxLayout.X_AXIS));
        blendProperty.add(new JLabel("Blend Mode:     "));
        blendComboBox = new JComboBox(BLEND_MODES) {
            @Override
            public void setSelectedItem(Object item) {
                if (item == null) return;
                if (item == "") getModel().setSelectedItem("");
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
        Dimension opacitySpinnerSize = new Dimension(85, 22);
        opacitySpinner.setMinimumSize(opacitySpinnerSize);
        opacitySpinner.setMaximumSize(opacitySpinnerSize);
        opacitySpinner.setPreferredSize(opacitySpinnerSize);
        opacitySpinner.setEditor(new JSpinner.NumberEditor(opacitySpinner, "##0.##'%'"));
        opacitySpinner.addChangeListener(this);
        opacityProperty.add(opacitySlider);
        opacityProperty.add(opacitySpinner);
        opacityProperty.setBorder(padding);
        
        //layer properties container
        JPanel layerProperties = new JPanel();
        layerProperties.setLayout(new BoxLayout(layerProperties, BoxLayout.Y_AXIS));
        layerProperties.add(nameProperty);
        layerProperties.add(blendProperty);
        layerProperties.add(opacityProperty);
        add(layerProperties, BorderLayout.NORTH);
        
        //LAYER LIST
        layerList = new JPanel(new GridBagLayout());
        layerList.setBackground(new Color(0x333333)); //probably shouldn't hardcode this but oh well
        JScrollPane scrollPane = new JScrollPane(layerList);
        add(scrollPane);
        
        //actions
        layerActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        add(layerActionsPanel, BorderLayout.SOUTH);
        Dimension buttonSize = new Dimension(24, 24);
        layerActionsPanel.setPreferredSize(new Dimension(0, buttonSize.height + 2));
        for (LayerAction action : layerActions) {
            JButton button = new JButton(action);
            button.setPreferredSize(buttonSize);
            button.setMinimumSize(buttonSize);
            button.setMaximumSize(buttonSize);
            button.setBorderPainted(false);
            button.setBackground(null);
            button.setOpaque(false);
            layerActionsPanel.add(button);
        }
        
        updateProperties();
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
        
        if (view.hasSelectedDocument()) {
            for (Layer layer : view.getSelectedDocument().getLayers()) {
                LayerView layerView = new LayerView(this, layer);
                layerGroup.add(layerView);
                layerList.add(layerView, layerConstraint, 0);
                if (layer == view.getSelectedDocumentView().getSelectedLayer()) {
                    layerView.setSelected(true);
                }
            }
        }
        updateProperties();
        updateLayerActionAbility();
        
        validate();
        repaint();
    }
    
    public void updateProperties() {
        boolean enabled = view.hasSelectedDocument();
        nameField.setEnabled(enabled);
        if (enabled) {
            Layer layer = view.getSelectedDocumentView().getSelectedLayer();
            nameField.setText(layer.getName());
            blendComboBox.setSelectedIndex(layer.getBlendIndex());
            opacitySpinner.setValue(layer.getOpacity() * 100d);
            
            enabled = enabled && layer.isVisible();
        } else {
            nameField.setText("");
            blendComboBox.setSelectedItem("");
            opacitySpinner.setValue(0);
            opacitySlider.setValue(0);
        }
        opacitySlider.setEnabled(enabled);
        opacitySpinner.setEnabled(enabled);
        blendComboBox.setEnabled(enabled);
    }
    
    public void updateLayerActionAbility() {
        for (Component c : layerActionsPanel.getComponents()) {
            c.setEnabled(((JButton) c).getAction().isEnabled());
        }
    }
    
    @Override //listener for opacity
    public void stateChanged(ChangeEvent e) {
        if (!view.hasSelectedDocument()) return;
        Object source = e.getSource();
        float opacity;
        if (source instanceof JSlider) {
            opacity = (float) opacitySlider.getValue() / 255f;
            opacitySpinner.removeChangeListener(this);
            opacitySpinner.setValue(opacity * 100d);
            opacitySpinner.addChangeListener(this);
        } else if (source instanceof JSpinner) {
            opacity = (float)((double)opacitySpinner.getValue() / 100d);
            opacitySlider.removeChangeListener(this);
            opacitySlider.setValue((int)(opacity * 255));
            opacitySlider.addChangeListener(this);
        } else {
            return;
        }
        DocumentView docView = view.getSelectedDocumentView();
        docView.getSelectedLayer().setOpacity(opacity);
        docView.getDocument().updateFlattenedCache();
        docView.getCanvas().repaint();
    }
    
    @Override //listener for blend mode
    public void actionPerformed(ActionEvent e) {
        if (!view.hasSelectedDocument()) return;
        JComboBox combo = (JComboBox) e.getSource();
        DocumentView docView = view.getSelectedDocumentView();
        Document doc = docView.getDocument();
        docView.getSelectedLayer().setBlendComposite(combo.getSelectedIndex());
        doc.updateFlattenedCache();
        docView.getCanvas().repaint();
    }
    
    @Override //listener for name
    public void changedUpdate(DocumentEvent e) {
        pushLayerName();
    }
    
    @Override //listener for name
    public void insertUpdate(DocumentEvent e) {
        pushLayerName();
    }
    
    @Override //listener for name
    public void removeUpdate(DocumentEvent e) {
        pushLayerName();
    }
    
    private void pushLayerName() {
        if (!view.hasSelectedDocument()) return;
        Layer layer = view.getSelectedDocumentView().getSelectedLayer();
        layer.setName(nameField.getText());
        getSelectedLayerView().updateName();
    }
    
    public View getView() {
        return view;
    }
    
    public LayerView getLayerView(Layer layer) {
        for (Component c : layerList.getComponents()) {
            if (!(c instanceof LayerView)) continue;
            LayerView layerView = (LayerView) c;
            if (layerView.getLinkedLayer() == layer) return layerView;
        }
        return null;
    }
    
    public LayerView getSelectedLayerView() {
        return getLayerView(view.getSelectedDocumentView().getSelectedLayer());
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
        
        layerLabel.setPreferredSize(new Dimension(64, 32));
        update();
        add(layerLabel);
        
        visibilityButton = new JToggleButton(INVISIBLE, layer.isVisible());
        visibilityButton.setSelectedIcon(VISIBLE);
        visibilityButton.addItemListener(this);
        visibilityButton.setPreferredSize(new Dimension(24, 24));
        visibilityButton.setContentAreaFilled(false);
        visibilityButton.setBorderPainted(false);
        
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new GridBagLayout());
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(visibilityButton);
        add(wrapper, BorderLayout.EAST);
    }
    
    public void update() {
        updateName();
        updateThumbnail();
    }
    
    public void updateThumbnail() {
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
    
    public void updateName() {
        layerLabel.setText(layer.getName());
    }
    
    @Override //layer selected
    public void actionPerformed(ActionEvent e) {
        parent.getView().getSelectedDocumentView().setSelectedLayer(layer);
        parent.updateProperties();
        parent.updateLayerActionAbility();
    }
    
    @Override //visibilityButton button state listener
    public void itemStateChanged(ItemEvent e) {
        layer.setVisible(visibilityButton.isSelected());
        parent.updateProperties();
        DocumentView docView = parent.getView().getSelectedDocumentView();
        docView.getDocument().updateFlattenedCache();
        docView.getCanvas().repaint();
    }
    
    public Layer getLinkedLayer() {
        return layer;
    }
}
private abstract class MenuBarAction extends AbstractAction {
    protected View view;
    private File file; //hacky, i know. but i wouldn't be able to reference it in promptFile() and i can't make it final.
    
    public MenuBarAction(View view, String name, Object accelerator) {
        this.view = view;
        putValue(NAME, name);
        KeyStroke acc = null;
        if (accelerator instanceof KeyStroke) acc = (KeyStroke) accelerator;
        if (accelerator instanceof String) acc = KeyStroke.getKeyStroke((String)accelerator);
        if (acc == null) return;
        putValue(ACCELERATOR_KEY, acc);
        //setEnabled(false);
    }
    
    public MenuBarAction(View view, String name) {
        this(view, name, null);
    }
    
    public void execute() {
        actionPerformed(new ActionEvent(view.getFrame(), ActionEvent.ACTION_FIRST, null));
    }
    
    protected File promptFile(final boolean isOpen) {
        final FileChooser fileChooser = new FileChooser();
        if (isOpen) fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Image Types", "*.png", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif", "*.bmp", ".dib", "*.wbmp", "*.gif"));
        fileChooser.getExtensionFilters().addAll(
            new ExtensionFilter("PNG", "*.png"),
            new ExtensionFilter("JPEG", "*.jpg", "*.jpeg", "*.jpe", "*.jif", "*.jfif"),
            new ExtensionFilter("BMP", "*.bmp", ".dib"),
            new ExtensionFilter("WBMP", "*.wbmp"),
            new ExtensionFilter("GIF", "*.gif"));
        //stop the main thread and freeze frame until a file is chosen
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                view.getFrame().setEnabled(false);
                file = isOpen ? fileChooser.showOpenDialog(null) : fileChooser.showSaveDialog(null);
                latch.countDown();
                view.getFrame().setAlwaysOnTop(true);
            }
        } );
        try {
            latch.await();
        } catch(InterruptedException ex) {
            throw new RuntimeException(ex);
        } finally {
            view.getFrame().setEnabled(true);
            view.getFrame().setAlwaysOnTop(false);
            return file;
        }
    }
    
    @Override
    public boolean isEnabled() {
        return view.hasSelectedDocument();
    }
}

//File Menu Actions
public class NewFileAction extends MenuBarAction {
    public NewFileAction(View view) {
        super(view, "New...", "ctrl N");
        setEnabled(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        int index = view.getImageTabs().getSelectedIndex() + 1;
        view.insertDocument(new Document(800, 600), index);
        view.getImageTabs().setSelectedIndex(index);
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}

public class OpenFileAction extends MenuBarAction {
    public OpenFileAction(View view) {
        super(view, "Open...", "ctrl O");
        setEnabled(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        File file = promptFile(true);
        if (file == null) return;
        int index = view.getImageTabs().getSelectedIndex() + 1;
        view.insertDocument(new Document(file), index);
        view.getImageTabs().setSelectedIndex(index);
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}

public class SaveAction extends MenuBarAction {
    private Document document;
    
    public SaveAction(View view, Document document) {
        super(view, "Save", "ctrl S");
        this.document = document;
    }
    
    public SaveAction(View view) {
        this(view, null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Document doc = document == null ? view.getSelectedDocument() : document;
        if (!doc.isLinked()) {
            new SaveAsAction(view, doc).execute();
            return;
        }
        File file = doc.getLinkedFile();
        try {
            ImageIO.write(doc.flattened(), "png", file);
            throw new IOException();
        } catch(IOException ioex) {
            JOptionPane.showMessageDialog(null, "Something went wrong when trying to save your file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        doc.setSaved(true);
    }
}

public class SaveAsAction extends MenuBarAction {
    private Document document;
    
    public SaveAsAction(View view, Document document) {
        super(view, "Save As...", "ctrl shift S");
        this.document = document;
    }
    
    public SaveAsAction(View view) {
        this(view, null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Document doc = document == null ? view.getSelectedDocument() : document;
        File file = promptFile(false);
        if (file == null) return;
        doc.setLinkedFile(file);
        new SaveAction(view, doc).execute();
    }
}

public class CloseFileAction extends MenuBarAction {
    private final String[] options = {"Save", "Don't Save", "Cancel"};
    private final int SAVE = 0, DONT_SAVE = 1, CANCEL = 2;
    private int index;
    private int response = 2;
    
    public CloseFileAction(View view, int index) {
        super(view, "Close", "ctrl W");
        this.index = index;
    }
    
    public CloseFileAction(View view) {
        this(view, - 1);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        int i = index == - 1 ? view.getImageTabs().getSelectedIndex() : index;
        JTabbedPane imageTabs = view.getImageTabs();
        DocumentView docView = (DocumentView) imageTabs.getComponentAt(i);
        if (!docView.getDocument().isSaved()) {
            response = JOptionPane.showOptionDialog(view.getFrame(),
                String.format("Save changes to \" % s\" before closing?", docView.getDocument().getName()),
                "Warning", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[SAVE]);
            
            switch(response) {
                case CANCEL: return;
                case SAVE: new SaveAction(view, docView.getDocument()).execute();
            }
        } else {
            response = SAVE;
        }
        
        imageTabs.remove(i);
    }

        public boolean getSuccess() {
                return response != CANCEL;
            }
        }
        
        public class CloseAllAction extends MenuBarAction {
            public CloseAllAction(View view) {
                super(view, "Close All", "ctrl alt W");
            }
            
            @Override
            public void actionPerformed(ActionEvent e) {
                final JTabbedPane imageTabs = view.getImageTabs();
                final int tabCount = imageTabs.getTabCount();
                for (int i = tabCount - 1; i >= 0; i--) {
                    imageTabs.setSelectedIndex(i);
                    CloseFileAction closeFileAction = new CloseFileAction(view, i);
                    closeFileAction.execute();
                    if (!closeFileAction.getSuccess()) {
                        break;
                }
            }
        }
    }
    
    public class CloseOtherAction extends MenuBarAction {
        public CloseOtherAction(View view) {
            super(view, "Close Others", "ctrl alt P");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            final JTabbedPane imageTabs = view.getImageTabs();
            final int tabCount = imageTabs.getTabCount();
            final int selected = imageTabs.getSelectedIndex();
            for (int i = tabCount - 1; i >= 0; i--) {
                if (i == selected) continue;
                imageTabs.setSelectedIndex(i);
                CloseFileAction closeFileAction = new CloseFileAction(view, i);
                closeFileAction.execute();
                if (!closeFileAction.getSuccess()) {
                    break;
            }
        }
    }
    
    @Override
    public boolean isEnabled() {
        if (view.getImageTabs() == null) return false;
        return view.getImageTabs().getTabCount() > 1;
    }
}

public class PrintAction extends MenuBarAction {
    Document doc;
    
    public PrintAction(View view) {
        this(view, null);
    }
    
    public PrintAction(View view, Document doc) {
        super(view, "Print...", "ctrl P");
        this.doc = doc;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (doc == null) doc = view.getSelectedDocument();
        if (doc == null) return;
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new PrintableImage(printJob, doc.flattened()));
        if (printJob.printDialog()) {
            try {
                printJob.print();
            } catch(PrinterException prt) {
                prt.printStackTrace();
            }
        }
    }
    
    public class PrintableImage implements Printable {
        private double x, y, width;
        private int orientation;
        private BufferedImage image;
        
        public PrintableImage(PrinterJob printJob, BufferedImage image) {
            PageFormat pageFormat = printJob.defaultPage();
            this.x = pageFormat.getImageableX();
            this.y = pageFormat.getImageableY();
            this.width = pageFormat.getImageableWidth();
            this.orientation = pageFormat.getOrientation();
            this.image = image;
        }
        
        @Override
        public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
            if (pageIndex == 0) {
                int pWidth = 0;
                int pHeight = 0;
                if (orientation == PageFormat.PORTRAIT) {
                    pWidth = (int) Math.min(width,(double) image.getWidth());
                    pHeight = pWidth * image.getHeight() / image.getWidth();
                } else {
                    pHeight = (int) Math.min(width,(double) image.getHeight());
                    pWidth = pHeight * image.getWidth() / image.getHeight();
                }
                g.drawImage(image,(int) x,(int) y, pWidth, pHeight, null);
                return PAGE_EXISTS;
            } else {
                return NO_SUCH_PAGE;
            }
        }
    }
}

public class ExitAction extends MenuBarAction {
    public ExitAction(View view) {
        super(view, "Exit", "ctrl Q");
        setEnabled(true);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        new CloseAllAction(view).execute();
        if (view.getImageTabs().getTabCount() == 0) {
            forceExit();
        }
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
}
//Edit Menu Actions
public class UndoAction extends MenuBarAction {
    public UndoAction(View view) {
        super(view, "Undo", "ctrl Z");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        view.getSelectedDocumentView().snapShotManager.undo();
        view.getLayerListView().update();
    }
    
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().snapShotManager.ableToUndo();
    }
}

public class RedoAction extends MenuBarAction {
    public RedoAction(View view) {
        super(view, "Redo", "ctrl shift Z");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        view.getSelectedDocumentView().getSnapShotManager().redo();
        view.getLayerListView().update();
    }
    
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().getSnapShotManager().ableToRedo();
    }
}


//View Menu Actions
public class ZoomInAction extends MenuBarAction {
    Point2D pos;
    
    public ZoomInAction(View view) {
        super(view, "Zoom In", KeyStroke.getKeyStroke('=', InputEvent.CTRL_DOWN_MASK));
    }
    
    public void setPosition(Point2D pos) {
        this.pos = pos;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        final float[] ZOOM_TABLE = docView.ZOOM_TABLE;
        int previousIndex = 0;
        float scale = docView.getScale();
        
        for (int i = 0; i < ZOOM_TABLE.length; i++) {
            if (ZOOM_TABLE[i] <= scale * 100) previousIndex = i;
        }
        if (!(previousIndex + 1 < ZOOM_TABLE.length)) return;
        float newScale = ZOOM_TABLE[previousIndex + 1] / 100;
        if (pos == null) {
            docView.setScale(newScale);
        } else {
            pos.setLocation(pos.getX() * scale, pos.getY() * scale);
            docView.setScale(newScale, pos);
            pos = null;
        }
    }
}

public class ZoomOutAction extends MenuBarAction {
    Point2D pos;
    
    public ZoomOutAction(View view) {
        super(view, "Zoom Out", KeyStroke.getKeyStroke('-', InputEvent.CTRL_DOWN_MASK));
    }
    
    public void setPosition(Point2D pos) {
        this.pos = pos;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        final float[] ZOOM_TABLE = docView.ZOOM_TABLE;
        int previousIndex = 0;
        float scale = docView.getScale();
        
        for (int i = 0; i < ZOOM_TABLE.length; i++) {
            if (ZOOM_TABLE[i] < scale * 100) previousIndex = i;
        }
        if (previousIndex < 1) return;
        float newScale = ZOOM_TABLE[previousIndex - 1] / 100;
        if (pos == null) {
            docView.setScale(newScale);
        } else {
            pos.setLocation(pos.getX() * scale, pos.getY() * scale);
            docView.setScale(newScale, pos);
            pos = null;
        }
    }
}

public class ZoomToWindowAction extends MenuBarAction {
    public ZoomToWindowAction(View view) {
        super(view, "Zoom to Window", "ctrl B");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        Document doc = docView.getDocument();
        Dimension extentSize = docView.getViewport().getExtentSize();
        docView.setScale(Math.min(
           (float)extentSize.width / doc.getWidth(),
           (float)extentSize.height / doc.getHeight()
           ));
    }
}

public class ZoomToSelectionAction extends MenuBarAction {
    public ZoomToSelectionAction(View view) {
        super(view, "Zoom to Selection", "ctrl shift B");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        DocumentView docView = view.getSelectedDocumentView();
        Rectangle selected = docView.getSelection().getBounds();
        Dimension extentSize = docView.getViewport().getExtentSize();
        docView.setScale(Math.min(
           (float)extentSize.width / (float)selected.width,
           (float)extentSize.height / (float)selected.height
           ));
        
        Rectangle scaledSelection = docView.getScaledSelection().getBounds();
        docView.getViewport().setViewPosition(new Point(
            scaledSelection.x - (extentSize.width - scaledSelection.width) / 2,
            scaledSelection.y - (extentSize.height - scaledSelection.height) / 2));
    }
    
    @Override
    public boolean isEnabled() {
        return super.isEnabled() && view.getSelectedDocumentView().hasSelection();
    }
}

public class ActualSizeAction extends MenuBarAction {
    public ActualSizeAction(View view) {
        super(view, "Actual Size", "ctrl 0");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        view.getSelectedDocumentView().setScale(1.0f);
    }
}

public class TogglePixelGrid extends MenuBarAction {
    public TogglePixelGrid(View view) {
        super(view, "Turn Pixel Grid On");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        boolean pixelGridEnabled = !view.isPixelGridEnabled();
        view.setPixelGridEnabled(pixelGridEnabled);
        if (pixelGridEnabled) {
            putValue(NAME, "Turn Pixel Grid Off");
        } else {
            putValue(NAME, "Turn Pixel Grid On");
        }
    }
}
class MultiBorderLayout extends BorderLayout {
    private final String[] SIDE_NAMES = {"North", "South", "West", "East", "Center"};
    private HashMap<String, Vector> sides = new HashMap<String, Vector>();
    
    public MultiBorderLayout(int hgap, int vgap) {
        super(hgap, vgap);
        //create a vector for each side
        for (String sideName : SIDE_NAMES) sides.put(sideName, new Vector());
    }
    
    public MultiBorderLayout() {
        this(0, 0);
    }
    
    public void addLayoutComponent(String name, Component component) {
        synchronized(component.getTreeLock()) { //make it so only one thread has access at one time
            name = name == null ? "Center" : name; // default to placing in the center
            try {
                sides.get(name).add(component);
            } catch(Exception e) {
                throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
            }
        }
    }
    
    public void removeLayoutComponent(Component component) {
        synchronized(component.getTreeLock()) { //make it so only one thread has access at one time
            for (String sideName : SIDE_NAMES) sides.get(sideName).remove(component); //iterate until we get it
        }
    }
    
    private Dimension getLayoutSize(Container target, boolean isPreferred) {
        synchronized(target.getTreeLock()) { //make it so only one thread has access at one time
            Dimension dim = new Dimension(0, 0);
            Component c;
            
            for (String sideName : SIDE_NAMES) {
                for (int i = 0; i < sides.get(sideName).size(); i++) {
                    c = (Component) sides.get(sideName).get(i);
                    if (!c.isVisible()) continue;
                    Dimension d = isPreferred ? c.getPreferredSize() : c.getMinimumSize();
                    
                    switch(sideName) {
                        case "North":
                            case"South" : //fill horizontally
                                dim.width = Math.max(d.width, dim.width);
                                dim.height += d.height + this.getVgap();
                                break;
                            case"East":
                               case "West" : //fill verically
                                    dim.width += d.width + this.getHgap();
                                    dim.height = Math.max(d.height, dim.height);
                                    break;
                               case "Center" : //fill centrally
                                    dim.width += d.width;
                                    dim.height = Math.max(d.height, dim.height);
                                    break;
                            }
                        }
                    }
                    
                    //add insets to the dimensions
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
                synchronized(target.getTreeLock()) { //make it so only one thread has access at one time
                    Insets insets = target.getInsets();
                    int top = insets.top;
                    int bottom = target.getHeight() - insets.bottom;
                    int left= insets.left;
                    int right = target.getWidth() - insets.right;
                    Component c;
                    
                    for (String sideName : SIDE_NAMES) {
                        for (int i = 0; i < sides.get(sideName).size(); i++) {
                            c = (Component) sides.get(sideName).get(i);
                            if (!c.isVisible()) continue;
                            Dimension d = c.getPreferredSize();
                            
                            switch(sideName) {
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
                                    right -=d.width;
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
        
class DragGesture {
    private Point2D start = new Point2D.Double(), last = new Point2D.Double(), current = new Point2D.Double(), end = new Point2D.Double();
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
        if (dragging) return null;
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
    
    public Set getButtons() {
        return buttons;
    }
    
    public void pressButton(int button) {
        buttons.add(button);
    }
    
    public void releaseButton(int button) {
        buttons.remove(button);
    }
}

public abstract class ToolAction extends AbstractAction {
    protected String name;
    protected DragGesture dragState = new DragGesture();
    protected Point2D start, last, current;
    protected DocumentView docView;
    protected Document doc;
    protected Set buttons;
    protected Layer selectedLayer;
    protected ColorSelector selector;
    protected Rectangle imageRect;
    private String toolTip;
    View view;
    
    ToolAction(String name, String toolIconName, View view) {
        this.name = name;
        this.view = view;
        toolTip = name;
        putValue(Action.SMALL_ICON, new ImageIcon(sketchPath(String.format("resources/tools/%s", toolIconName))));
    }
    
    public String getName() {
        return name;
    }
    
    protected void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }
    
    public String getToolTip() {
        return toolTip;
    }
    
    public ImageIcon getIcon() {
        return(ImageIcon) getValue(Action.SMALL_ICON);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (view.hasSelectedDocument()) {
            ImageIcon icon = (ImageIcon) getValue(Action.SMALL_ICON);
            view.getSelectedDocumentView().setToolTipIcon(icon);
        }
    }
    
    public DragGesture getDragState() {
        return dragState;
    }
    
    public void dragStarted() {}
    
    public void dragging() {}
    
    public void initVars() {
        start = dragState.getStart();
        current = dragState.getCurrent();
        last = dragState.getLast();
        buttons = dragState.getButtons();
        docView = view.getSelectedDocumentView();
        doc = docView.getDocument();
        selectedLayer = docView.getSelectedLayer();
        selector = view.getToolBar().getColorSelector();
        imageRect = new Rectangle(0, 0, doc.getWidth(), doc.getHeight());
    }
    
    public void dragEnded() {
    }
    
    public void click(Point2D pos, int button) {}
    
    protected void updateDocument() {
        docView.getDocument().updateFlattenedCache();
        docView.getCanvas().repaint();
        doc.setSaved(false);
        view.getLayerListView().update();
    }
    
    protected Color getSelectedColor() {
        if (buttons.contains(MouseEvent.BUTTON1))
            return selector.getPrimary();
        if (buttons.contains(MouseEvent.BUTTON3))
            return selector.getSecondary();
        return null;
    }
}

public class MoveAction extends ToolAction {
    MoveAction(View view) {
        super("Move Tool", "move.png", view);
    }
    
    public void dragging() {
        
    }
    
    public void dragEnded() {
    }
}

public class SelectAction extends ToolAction {
    SelectAction(View view) {
        super("Rectangle Select Tool", "select.png", view);
    }
    
    public void dragging() {
        super.initVars();
        if (!dragState.isDragging()) return; //check if just a click
        
        int startX = (int) start.getX();
        int startY = (int) start.getY();
        int width = (int)current.getX() - startX + 1;
        int height = (int)current.getY() - startY + 1;
        
        Rectangle selection = new Rectangle(
            startX + (width <= 0 ? --width : 0),
            startY + (height <= 0 ? --height : 0),
            Math.abs(width),
            Math.abs(height)
           ).intersection(imageRect);
        
        if (selection.height == 0 || selection.width == 0) return;
        docView.setSelection(selection);
    }
    
    @Override
    public void click(Point2D pos, int button) {
        docView = view.getSelectedDocumentView();
        docView.setSelection(null);
    }
}

public class CropAction extends ToolAction {
    CropAction(View view) {
        super("Crop Tool", "crop.png", view);
    }
    
    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }
    
    public void dragging() {
        super.initVars();
        if (!dragState.isDragging()) return; //check if just a click
        
        int startX = (int) start.getX();
        int startY = (int) start.getY();
        int width = (int)current.getX() - startX + 1;
        int height = (int)current.getY() - startY + 1;
        
        Rectangle selection = new Rectangle(
            startX + (width <= 0 ? --width : 0),
            startY + (height <= 0 ? --height : 0),
            Math.abs(width),
            Math.abs(height))
           .intersection(imageRect);
        
        if (selection.height == 0 || selection.width == 0) return;
        docView.setSelection(selection);
    }
    
    public void dragEnded() {
        doc.crop(docView.getSelection().getBounds());
        docView.setSelection(null);
        docView.getCanvas().revalidate();
        docView.updateImageSizeLabel();
        view.getLayerListView().update();
    }
}

public class EyeDropAction extends ToolAction {
    EyeDropAction(View view) {
        super("Eyedropper Tool", "eyedrop.png", view);
    }
    
    public void dragging() {
        super.initVars();
        if (!imageRect.contains(current)) return;
        
        BufferedImage samplingImage = selectedLayer.getImage();
        Color c = new Color(samplingImage.getRGB((int) current.getX(),(int) current.getY()), true);
        
        if (buttons.contains(MouseEvent.BUTTON1))
            selector.setPrimary(c);
        if (buttons.contains(MouseEvent.BUTTON3))
            selector.setSecondary(c);
    }
}

public class BrushAction extends ToolAction {
    BrushAction(View view) {
        super("Paintbrush Tool", "brush.png", view);
    }
    
    public void dragging() {
        super.initVars();
        if (!selectedLayer.isVisible()) return;
        Graphics2D g = selectedLayer.getGraphics();
        g.setClip(docView.getSelection());
        g.setPaint(getSelectedColor());
        g.setStroke(new BasicStroke(100, BasicStroke.CAP_ROUND, 0));
        g.draw(new Line2D.Double(last.getX(), last.getY(), current.getX(), current.getY()));
        
        updateDocument();
    }
    
    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }
}

public class PencilAction extends ToolAction {
    PencilAction(View view) {
        super("Pencil", "pencil.png", view);
    }
    
    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }
    
    public void dragging() {
        super.initVars();
        if (!selectedLayer.isVisible()) return;
        //i should add a commit layer but im running out of time
        Graphics2D g = selectedLayer.getGraphics();
        g.setClip(docView.getSelection());
        g.setPaint(getSelectedColor());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setStroke(new BasicStroke(1));
        g.drawLine((int)last.getX(),(int)last.getY(),(int)current.getX(),(int)current.getY());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        updateDocument();
        
    }
}

public class EraserAction extends ToolAction {
    EraserAction(View view) {
        super("Eraser", "eraser.png", view);
    }
    
    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }
    
    public void dragging() {
        super.initVars();
        if (!selectedLayer.isVisible()) return;
        Graphics2D g = selectedLayer.getGraphics();
        g.setClip(docView.getSelection());
        g.setStroke(new BasicStroke(100, BasicStroke.CAP_ROUND, 0));
        Composite before = g.getComposite();
        g.setComposite(AlphaComposite.Clear);
        g.draw(new Line2D.Double(last.getX(), last.getY(), current.getX(), current.getY()));
        g.setComposite(before);
        
        updateDocument();
    }
}

public class FillAction extends ToolAction {
    FillAction(View view) {
        super("Paint Bucket Tool", "fill.png", view);
    }
    
    public void dragStarted() {
        view.getSelectedDocumentView().snapShotManager.save();
    }
    
    public void dragging() {
        super.initVars();
        if (!selectedLayer.isVisible()) return;
        if (!docView.getSelection().contains(current)) return;
        
        Graphics2D g = selectedLayer.getGraphics();
        g.setClip(null);
        g.setPaint(getSelectedColor());
        g.fill(docView.getSelection());
        updateDocument();
    }
}

public class TextAction extends ToolAction {
    TextAction(View view) {
        super("Text Tool", "text.png", view);
    }
    
    public void dragging() {
        if (!selectedLayer.isVisible()) return;
    }
}

public class PanAction extends ToolAction {
    PanAction(View view) {
        super("Pan Tool", "pan.png", view);
    }
    
    public void dragging() {
        DocumentView docView = view.getSelectedDocumentView();
        JViewport viewport = docView.getViewport();
        Point2D start = dragState.getStart();
        Point2D current = dragState.getCurrent();
        float scale = docView.getScale();
        
        Double deltaX = scale * start.getX() - scale * current.getX();
        Double deltaY = scale * start.getY() - scale * current.getY();
        
        Rectangle view = viewport.getViewRect();
        view.x += Math.round(deltaX);
        view.y += Math.round(deltaY);
        
       ((JPanel)viewport.getView()).scrollRectToVisible(view);
    }
}

public class ZoomAction extends ToolAction {
    ZoomInAction zoomInAction;
    ZoomOutAction zoomOutAction;
    
    ZoomAction(View view) {
        super("Zoom Tool", "zoom.png", view);
        zoomInAction = new ZoomInAction(view);
        zoomOutAction = new ZoomOutAction(view);
    }
    
    public void click(Point2D pos, int button) {
        switch(button) {
            case MouseEvent.BUTTON1:
                zoomInAction.setPosition(pos);
                zoomInAction.execute();
                break;
            case MouseEvent.BUTTON3:
                zoomOutAction.setPosition(pos);
                zoomOutAction.execute();
                break;
        }
    }
}
public abstract class StyledJToolBar extends JToolBar {
    StyledJToolBar() {
        //when parent changes and floating, set toolbar frame to undecorated
        //because minimum native frame width is too wide, and also looks better
        addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                   JToolBar toolbar = (JToolBar) e.getComponent();
                   if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) == 0) return;
                   if (!((BasicToolBarUI) toolbar.getUI()).isFloating()) return;
                   Window window = SwingUtilities.windowForComponent(toolbar);
                   if (window == null) return;
                   window.dispose();
                  ((JDialog) window).setUndecorated(true);
                   window.setVisible(true);
            }
        } );
        
        //add border to stand out
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x2B2B2B)),
            getBorder()
           ));
    }
    
    public void addRigidSpace(int length) {
        add(Box.createRigidArea(new Dimension(length, length)));
    }
}

public class ToolBar extends StyledJToolBar {
    private ArrayList<ButtonModel> buttonModelList = new ArrayList<ButtonModel>();
    private ColorSelector selector = new ColorSelector();
    private ButtonGroup group = new ButtonGroup();
    private int selectedToolIndex = 0;
    private ToolAction[] toolActions;
    
    ToolBar(ToolAction[] toolActions) {
        super();
        this.toolActions = toolActions;
        setOrientation(JToolBar.VERTICAL);
        
        addRigidSpace(8);
        add(selector);
        addRigidSpace(8);
        
        for (ToolAction tool : toolActions) {
            JToggleButton button = new JToggleButton(tool);
            Dimension size = new Dimension(32, 24);
            button.setPreferredSize(size);
            button.setMaximumSize(size);
            button.setMinimumSize(size);
            button.setAlignmentX(CENTER_ALIGNMENT);
            group.add(button);
            add(button);
            buttonModelList.add(button.getModel());
        }
        
        setSelectedTool(selectedToolIndex);
    }
    
    public void setSelectedTool(int index) {
        selectedToolIndex = index;
        group.setSelected(buttonModelList.get(index), true);
    }
    
    public ToolAction getSelectedTool() {
        return toolActions[getSelectedIndex()];
    }
    
    public int getSelectedIndex() {
        return buttonModelList.indexOf(group.getSelection());
    }
    
    public ColorSelector getColorSelector() {
        return selector;
    }
}

public class ToolOptions extends StyledJToolBar implements ActionListener {
    JComboBox toolsCombo;
    ToolBar toolBar;
    
    ToolOptions(ToolBar toolBar) {
        this.toolBar = toolBar;
        setPreferredSize(new Dimension(32, 32));
        
        toolsCombo = new JComboBox();
        for (Component c : toolBar.getComponents()) {
            try {
                JToggleButton button = (JToggleButton) c;
                button.addActionListener(this);
                toolsCombo.addItem((ToolAction) button.getAction());
            } catch(Exception e) {}
        }
        toolsCombo.setRenderer(new ComboBoxRenderer());
        toolsCombo.setBackground(null);
        toolsCombo.setOpaque(false);
        toolsCombo.setMaximumSize(new Dimension(55, 24));
        toolsCombo.setPreferredSize(new Dimension(55, 24));
        toolsCombo.setFocusable(false);
        toolsCombo.addActionListener(this);
        add(toolsCombo);
        add(Box.createRigidArea(new Dimension(5, 5)));
        add(new JSeparator(JSeparator.VERTICAL));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox) {
            toolBar.setSelectedTool(toolsCombo.getSelectedIndex());
        } else {
            toolsCombo.setSelectedIndex(toolBar.getSelectedIndex());
        }
        
    }
}


class ComboBoxRenderer extends JLabel implements ListCellRenderer {
    private JSeparator separator;
    
    public ComboBoxRenderer() {
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        separator = new JSeparator(JSeparator.HORIZONTAL);
    }
    
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value == null) return separator;
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
public class View extends JPanel {
    public final Color CONTENT_BACKGROUND = new Color(0x282828); //i could probably put this constant in a different static class
    private final JFXPanel JFXPANEL = new JFXPanel(); //this is needed for the FileChoosers
    private boolean drawPixelGrid = false;
    
    private JFrame frame;
    private JMenuBar menuBar;
    private ToolOptions toolOptions;
    private ToolBar toolBar;
    private JSplitPane splitPane;
    private LayerListView layerListView;
    private JTabbedPane imageTabs;
    private ToolAction[] toolActions = {
        //new MoveAction(this),
        new SelectAction(this),
            new CropAction(this),
            new EyeDropAction(this),
            new BrushAction(this),
            new PencilAction(this),
            new EraserAction(this),
            new FillAction(this),
            //new TextAction(this),
            new PanAction(this),
            new ZoomAction(this)
        };
    
    View(final JFrame frame) {
        //injects itself int to the frame, when it's safe to do so
        this.frame = frame;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setContentPane(initView());
                frame.revalidate();
            }
        } );
    }
    
    private View initView() {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        final ExitAction exitAction = new ExitAction(this);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitAction.execute();
            }
        } );
        setLayout(new MultiBorderLayout());
        
        setupMenuBar();
        setupToolBars();
        setupSplitPane();
        
        return this;
    }
    
    private void addMenuActions(JMenu menu, MenuBarAction[] actions) {
        //makes it easier to add custom MenuBarAction rather than repeat the same line over and over.
        //also creates anonymous class that overrides JMenuItem to set their enabled status without the use of listeners
        //i will admit, it took me a while to find out i also had to set the underlying model's enabled status to work
        menuBar.add(menu);
        
        for (MenuBarAction action : actions) {
            if (action == null) {
                menu.addSeparator();
                continue;
            }
            menu.add(new JMenuItem(action) {
                @Override
                public boolean isEnabled() { //makes menuActions always refer to the MenuBarAction to get it's enabled status
                    if (getAction() == null) return super.isEnabled();
                    boolean enabled = getAction().isEnabled();
                    if (enabled == false) setArmed(false);
                    getModel().setEnabled(enabled);
                    return enabled;
                }
            } );
        }
    }
    
    private void setupMenuBar() {
        frame.setJMenuBar(menuBar = new JMenuBar());
        addMenuActions(new JMenu("File"), new MenuBarAction[] {
            new NewFileAction(this),
                new OpenFileAction(this), null,
                new SaveAction(this),
                new SaveAsAction(this),
                null,
                new CloseFileAction(this),
                new CloseAllAction(this),
                new CloseOtherAction(this),
                null,
                new PrintAction(this),
                null,
                new ExitAction(this)} );
        addMenuActions(new JMenu("Edit"), new MenuBarAction[] {
            new UndoAction(this),
                new RedoAction(this)} );
        addMenuActions(new JMenu("View"), new MenuBarAction[] {
            new ZoomInAction(this),
                new ZoomOutAction(this),
                new ZoomToWindowAction(this),
                new ZoomToSelectionAction(this),
                new ActualSizeAction(this),
                null,
                new TogglePixelGrid(this)} );
        addMenuActions(new JMenu("Image"), new MenuBarAction[] {} );
        addMenuActions(new JMenu("Layer"), new MenuBarAction[] {} );
        addMenuActions(new JMenu("Filter"), new MenuBarAction[] {} );
    }
    
    private void setupToolBars() {
        add(toolBar = new ToolBar(toolActions), BorderLayout.WEST);
        add(toolOptions = new ToolOptions(toolBar), BorderLayout.NORTH);
    }
    
    private void setupSplitPane() {
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(1.0f);
        splitPane.setBackground(CONTENT_BACKGROUND);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
        setupLayerListView();
        setupImageTabs();
    }
    
    private void setupLayerListView() {
        layerListView = new LayerListView(this);
        layerListView.setPreferredSize(layerListView.getMinimumSize());
        splitPane.setRightComponent(layerListView);
    }
    
    private void setupImageTabs() {
        imageTabs = new DnDTabbedPane();
        imageTabs.setMinimumSize(new Dimension(0, 0));
        imageTabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        imageTabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                splitPane.setBackground(imageTabs.getTabCount() == 0 ? CONTENT_BACKGROUND : frame.getContentPane().getBackground());
                layerListView.update();
            }
        } );
        splitPane.setLeftComponent(imageTabs);
    }
    
    public boolean hasSelectedDocument() {
        if (imageTabs == null) return false;
        return imageTabs.getSelectedComponent() != null;
    }
    
    public DocumentView getSelectedDocumentView() {
        if (imageTabs == null) return null;
        return(DocumentView) imageTabs.getSelectedComponent();
    }
    
    public Document getSelectedDocument() {
        DocumentView docView = getSelectedDocumentView();
        if (docView == null) return null;
        return docView.getDocument();
    }
    
    public DocumentView insertDocument(Document doc, int index) {
        DocumentView docView =    new DocumentView(doc, this);
        docView.setCanvasBackground(CONTENT_BACKGROUND);
        imageTabs.insertTab(doc.getName(), null, docView, null, index);
        return docView;
    }
    
    public DocumentView addDocument(Document doc) {
        //not really used but its here for future proofing
        return insertDocument(doc, imageTabs.getTabCount());
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    public ToolOptions getToolOptions() {
        return toolOptions;
    }
    
    public ToolBar getToolBar() {
        return toolBar;
    }
    
    public JTabbedPane getImageTabs() {
        return imageTabs;
    }
    
    public LayerListView getLayerListView() {
        return layerListView;
    }
    
    public boolean isPixelGridEnabled() {
        return drawPixelGrid;
    }
    public void setPixelGridEnabled(boolean value) {
        drawPixelGrid = value;
    }
}
  public void settings() {  size(800, 600); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "PhotoEditor" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
