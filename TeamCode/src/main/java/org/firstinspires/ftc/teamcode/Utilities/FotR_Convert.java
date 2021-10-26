package org.firstinspires.ftc.teamcode.Utilities;

/**
 * Created by Jack on 11/25/2017.
 */

public class FotR_Convert {
    //Both variables are set to be in inches
    private final double circum = 11.87;
    private double dist;
    //Pulses per revulusion on a NeveRest 20:1
    private final double ppr = 140;

    public double toEncode (double inches) {
        dist = inches;
        return dist / circum * ppr;

    }
    public double toInches (double encodeC) {
        dist = encodeC;
        return circum / dist * ppr;
    }
}
