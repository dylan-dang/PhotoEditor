package utils;

import javax.swing.UIDefaults;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import controller.composites.*;

public class Constants {
    public static final BlendComposite[] BLEND_MODES = new BlendComposite[] {new NormalComposite(),
            null, new DarkenComposite(), new MultiplyComposite(), new ColorBurnComposite(),
            new SubtractiveComposite(), null, new LightenComposite(), new ScreenComposite(),
            new ColorDodgeComposite(), new AdditiveComposite(), null, new OverlayComposite(),
            new SoftLightComposite(), new HardLightComposite(), new VividLightComposite(),
            new LinearLightComposite(), new PinLightComposite(), new HardMixComposite(), null,
            new DifferenceComposite(), new ExclusionComposite(), new DivideComposite(), null,
            new XorComposite(), new AndComposite(), new OrComposite()};

    public static enum SetOps {
        REPLACE("Replace"), ADD("Add (Union)"), SUBTRACT("Subtract"), INTERSECT(
                "Intersect"), INVERT("Invert (xor)");

        private final String display;

        private SetOps(String s) {
            display = s;
        }

        @Override
        public String toString() {
            return display;
        }
    };

    public static JComboBox<SetOps> setOpsComboBox = new JComboBox<SetOps>(SetOps.values());
    public static JSpinner weightSpinner =
            new JSpinner(new SpinnerNumberModel(20f, 1f, 10000f, 1f));
    public static JToggleButton antialiasingButton = new JToggleButton("", true);
    public static JComboBox<String> sampling =
            new JComboBox<String>(new String[] {"Layer", "Image"});
    public static final UIDefaults emptySelectedStyle = new UIDefaults(new Object[] {
            "ToolBar:ToggleButton[Selected].backgroundPainter", DrawingHelper.EMPTY_PAINTER});
}
