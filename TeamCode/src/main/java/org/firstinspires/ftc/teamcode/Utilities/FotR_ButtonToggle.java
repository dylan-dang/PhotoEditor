package org.firstinspires.ftc.teamcode.Utilities;

import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * Created by Jack on 11/18/2017.
 */

//Function that stores a boolean that can be read in place of a button.
//Using a timer, the boolean is only changed when the button is pressed after a certain interval has passed since the last press
public class FotR_ButtonToggle {
    boolean toggleOn = false;
    boolean Bstate = false;
    ElapsedTime lastpress = new ElapsedTime();

    public void toggle (boolean buttonState) {
        Bstate = buttonState;
        if (Bstate && lastpress.milliseconds() >= 250) {
            toggleOn = !toggleOn;
            lastpress.reset();
        }

    }
    public boolean isToggled () {
        return toggleOn;
    }
    public void setToggle (boolean toggle) {toggleOn = toggle;}
}
