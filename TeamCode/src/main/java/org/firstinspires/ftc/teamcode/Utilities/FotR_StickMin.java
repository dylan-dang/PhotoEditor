package org.firstinspires.ftc.teamcode.Utilities;

//Two variation of a method that, whenever a stick value is under a certain number, returns the minimum value set
public class FotR_StickMin {

    //Older version of the StickMin function, newer improved version below
    static public float StickMin (float stick, double powMulti, double minimum) {
        return (float) (stick * (powMulti + (minimum * Math.signum(stick))));
    }

    static public double StickMin (double stick, double minimum) {
        return (Math.abs(stick) < minimum) ? minimum * Math.signum(stick) : stick;
    }
}
