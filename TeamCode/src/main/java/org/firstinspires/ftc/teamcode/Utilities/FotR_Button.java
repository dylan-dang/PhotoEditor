package org.firstinspires.ftc.teamcode.Utilities;

import com.qualcomm.robotcore.util.ElapsedTime;

//Essentially a better version of Toggle that has a fwe extra functions and could be expanded upon more
public class FotR_Button {

    private FotR_ButtonToggle toggle;
    boolean bState = false;
    boolean isPressed;
    boolean singlePrs;

    //Looks at the button's state from the previous loop, and only returns a true for the first time the button is pressed, ignoring any other point
    public FotR_Button(boolean singlePress) {
        toggle = new FotR_ButtonToggle();
        toggle.setToggle(false);
        singlePrs = singlePress;
        isPressed = false;
    }
    //To pass the current controller input
    public void updateState(boolean button) {
        toggle.toggle(button);
        if (singlePrs) {
            if (isPressed && button) {
                bState = false;
            }
            else {
                bState = button;
            }
            isPressed = button;
        }
    }

    //For making any manual changes needed
    public void alterState(boolean button) {
        toggle.setToggle(button);
    }

    //For using button input
    public boolean returnState() {
        if (singlePrs) {
            return bState;
        }
        else {
            return toggle.isToggled();
        }
    }
    public boolean returnAltState() {
        if (singlePrs) {
            return toggle.isToggled();
        }
        else {
            return bState;
        }
    }


}
